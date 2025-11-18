#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generate product cover images via Ali DashScope and update the SQLite DB.

This script mirrors util/update_scenic_covers.py but targets the `product` table:
1. Read product records (id/name/description/cover_image) from the SQLite DB.
2. Build a prompt from product name + description to request DashScope image generation.
3. Download every image, convert it to JPG, and store it under static/productCover/.
4. Update product.cover_image with https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/{商品名}.jpg.

Usage examples (run from the project root):
    python util/update_product_covers.py --mode test --product-id 1
    python util/update_product_covers.py --mode full --skip-existing
"""

from __future__ import annotations

import argparse
import json
import logging
import os
import re
import sqlite3
import sys
import time
from io import BytesIO
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Tuple

import requests
from PIL import Image, UnidentifiedImageError
from requests import Session
from requests.exceptions import HTTPError

PROJECT_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_DB_PATH = PROJECT_ROOT / "db" / "TravelMap.db"
DEFAULT_COVER_DIR = PROJECT_ROOT / "static" / "productCover"
DEFAULT_BASE_URL = "https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage/"
TARGET_EXTENSION = ".jpg"

DASHSCOPE_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis"
DASHSCOPE_ALLOWED_SIZES = {"1024*1024", "720*1280", "1280*720", "768*1152"}
DASHSCOPE_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/{task_id}"

PROMPT_TEMPLATE = (
    "为旅行商品「{name}」生成一张高清写实风格、横版 16:9 构图的封面图。"
    "画面需要具备真实光影、商业宣传质感和统一的 3:2 比例视觉，适合作为商品封面。"
    "突出商品的核心卖点：{description}。画面干净、无文字和 logo。"
)
PROMPT_FALLBACK_DESCRIPTION = "强调旅途体验、舒适住宿及当地特色氛围，使其具备吸引力。"
NEGATIVE_PROMPT = "低质量, 模糊, 水印, 文字, logo, AI 痕迹, 漫画, 过度卡通, 变形, 噪点"

logger = logging.getLogger("product-cover-updater")


class ProductRecord(dict):
    """Type hint helper for rows returned from sqlite with row_factory=dict_factory."""

    id: int
    name: str
    description: Optional[str]
    cover_image: Optional[str]


def dict_factory(cursor, row):
    return {col[0]: row[idx] for idx, col in enumerate(cursor.description)}


def ensure_directory(path: Path) -> None:
    path.mkdir(parents=True, exist_ok=True)


def sanitize_filename(name: str, product_id: int) -> str:
    """
    Remove characters that cannot appear in file names but preserve Chinese text.
    """
    candidate = re.sub(r"[\\/:*?\"<>|#]+", "_", name.strip())
    candidate = re.sub(r"\s+", "_", candidate)
    candidate = candidate.strip("_")
    if not candidate:
        candidate = f"product_{product_id}"
    return candidate


def normalize_style(style: Optional[str]) -> str:
    if not style:
        return "<photography>"
    value = style.strip()
    if not value.startswith("<"):
        value = "<" + value
    if not value.endswith(">"):
        value = value + ">"
    return value


class ProgressBar:
    def __init__(self, total: int, width: int = 40) -> None:
        self.total = max(total, 1)
        self.width = width

    def update(self, current: int) -> None:
        ratio = min(max(current / self.total, 0.0), 1.0)
        filled = int(self.width * ratio)
        bar = "#" * filled + "-" * (self.width - filled)
        percent = ratio * 100
        sys.stdout.write(f"\rProgress: |{bar}| {percent:6.2f}% ({current}/{self.total})")
        sys.stdout.flush()
        if current >= self.total:
            sys.stdout.write("\n")
            sys.stdout.flush()


def build_file_name(
    name: str,
    product_id: int,
    cover_dir: Path,
    allow_overwrite: bool,
) -> Tuple[str, Path]:
    base = sanitize_filename(name, product_id)
    file_name = f"{base}{TARGET_EXTENSION}"
    target_path = cover_dir / file_name
    suffix = 1
    while target_path.exists() and not allow_overwrite:
        file_name = f"{base}_{suffix}{TARGET_EXTENSION}"
        target_path = cover_dir / file_name
        suffix += 1
    return file_name, target_path


def call_dashscope_image(
    session: Session,
    api_key: str,
    prompt: str,
    *,
    negative_prompt: str,
    model: str,
    size: str,
    style: str,
    poll_interval: int,
    poll_timeout: int,
) -> Dict[str, str]:
    payload = {
        "model": model,
        "input": {
            "prompt": prompt,
            "negative_prompt": negative_prompt,
        },
        "parameters": {
            "size": size,
            "style": style,
            "n": 1,
        },
    }
    headers = {
        "Authorization": f"Bearer {api_key}",
        "Content-Type": "application/json",
        "X-DashScope-Async": "enable",
    }
    logger.debug("Submitting DashScope task payload=%s", json.dumps(payload, ensure_ascii=False))
    response = session.post(DASHSCOPE_URL, headers=headers, json=payload, timeout=30)
    raise_for_status_verbose(response)
    data = response.json()
    return extract_result_or_poll(session, api_key, data, poll_interval, poll_timeout)


def extract_result_or_poll(
    session: Session,
    api_key: str,
    payload: Dict,
    poll_interval: int,
    poll_timeout: int,
) -> Dict[str, str]:
    output = payload.get("output") or {}
    task_status = output.get("task_status")
    results = output.get("results") or []
    if task_status == "SUCCEEDED" and results:
        return results[0]
    task_id = output.get("task_id")
    if not task_id:
        raise RuntimeError(f"DashScope 返回异常: {payload}")
    return poll_dashscope_task(session, api_key, task_id, poll_interval, poll_timeout)


def poll_dashscope_task(
    session: Session,
    api_key: str,
    task_id: str,
    poll_interval: int,
    poll_timeout: int,
) -> Dict[str, str]:
    headers = {
        "Authorization": f"Bearer {api_key}",
    }
    url = DASHSCOPE_TASK_URL.format(task_id=task_id)
    deadline = time.monotonic() + poll_timeout
    while time.monotonic() < deadline:
        resp = session.get(url, headers=headers, timeout=15)
        raise_for_status_verbose(resp)
        payload = resp.json()
        output = payload.get("output") or {}
        task_status = output.get("task_status")
        logger.debug("Polling task=%s status=%s", task_id, task_status)
        results = output.get("results") or []
        if task_status == "SUCCEEDED" and results:
            return results[0]
        if task_status in {"FAILED", "CANCELLED", "CANCELED"}:
            raise RuntimeError(f"DashScope 任务失败 {task_id}: {payload}")
        time.sleep(poll_interval)
    raise TimeoutError(f"DashScope 任务 {task_id} 超时（>{poll_timeout}s）")


def build_prompt(name: str, description: Optional[str]) -> str:
    clean_desc = (description or "").strip()
    clean_desc = re.sub(r"\s+", " ", clean_desc)
    if not clean_desc:
        clean_desc = PROMPT_FALLBACK_DESCRIPTION
    return PROMPT_TEMPLATE.format(name=name, description=clean_desc)


def convert_to_jpeg_image(image: Image.Image) -> Image.Image:
    """
    Convert arbitrary mode to RGB for consistent JPG output.
    """
    if image.mode == "RGB":
        return image
    if image.mode in {"RGBA", "LA"}:
        rgba = image.convert("RGBA")
        background = Image.new("RGB", rgba.size, (255, 255, 255))
        alpha = rgba.getchannel("A")
        background.paste(rgba.convert("RGB"), mask=alpha)
        return background
    return image.convert("RGB")


def download_and_save_jpeg(session: Session, url: str, target_path: Path) -> None:
    with session.get(url, timeout=120) as resp:
        raise_for_status_verbose(resp)
        payload = resp.content
    try:
        with Image.open(BytesIO(payload)) as img:
            jpeg_ready = convert_to_jpeg_image(img)
            jpeg_ready.save(target_path, format="JPEG", quality=95)
    except UnidentifiedImageError as exc:
        raise RuntimeError(f"无法解析 DashScope 返回的图片数据: {exc}") from exc


def update_cover_image(conn: sqlite3.Connection, product_id: int, new_url: str) -> None:
    cur = conn.cursor()
    cur.execute("UPDATE product SET cover_image = ? WHERE id = ?", (new_url, product_id))
    conn.commit()


def fetch_product_rows(
    conn: sqlite3.Connection,
    *,
    product_id: Optional[int],
    limit: Optional[int],
) -> List[ProductRecord]:
    cur = conn.cursor()
    sql = "SELECT id, name, description, cover_image FROM product"
    params: List = []
    if product_id is not None:
        sql += " WHERE id = ?"
        params.append(product_id)
    sql += " ORDER BY id ASC"
    if limit is not None:
        sql += f" LIMIT {int(limit)}"
    cur.execute(sql, params)
    rows = cur.fetchall()
    return rows


def should_skip_row(
    row: ProductRecord,
    *,
    base_url: str,
    cover_dir: Path,
    skip_existing: bool,
) -> bool:
    if not skip_existing:
        return False
    existing_url = (row.get("cover_image") or "").strip()
    if existing_url and existing_url.startswith(base_url):
        relative = existing_url.replace(base_url, "", 1)
        if (cover_dir / relative).exists():
            logger.info(
                "Skipping product id=%s name=%s (already has cover %s)",
                row["id"],
                row["name"],
                existing_url,
            )
            return True
    return False


def process_product(
    session: Session,
    conn: sqlite3.Connection,
    row: ProductRecord,
    *,
    api_key: str,
    args: argparse.Namespace,
) -> str:
    product_id = row["id"]
    product_name = row["name"]

    if should_skip_row(
        row,
        base_url=args.base_url,
        cover_dir=args.cover_dir,
        skip_existing=args.skip_existing,
    ):
        return "skipped"

    prompt = build_prompt(product_name, row.get("description"))
    logger.info("Requesting new cover for product #%s «%s»", product_id, product_name)

    if args.dry_run:
        logger.info("Dry-run: prompt=%s", prompt)
        return "skipped"

    try:
        result = call_dashscope_image(
            session,
            api_key,
            prompt,
            negative_prompt=NEGATIVE_PROMPT,
            model=args.model,
            size=args.size,
            style=args.style,
            poll_interval=args.poll_interval,
            poll_timeout=args.poll_timeout,
        )
    except Exception as exc:  # noqa: BLE001
        logger.exception("DashScope 请求失败 product=%s reason=%s", product_id, exc)
        return "failed"

    remote_url = result.get("url")
    if not remote_url:
        logger.error("任务成功但未返回 url product=%s payload=%s", product_id, result)
        return "failed"

    file_name, target_path = build_file_name(
        product_name,
        product_id,
        args.cover_dir,
        args.overwrite_files,
    )
    new_cover_url = args.base_url.rstrip("/") + "/" + file_name

    try:
        download_and_save_jpeg(session, remote_url, target_path)
    except Exception as exc:  # noqa: BLE001
        logger.exception("下载或转换失败 product=%s url=%s reason=%s", product_id, remote_url, exc)
        return "failed"

    update_cover_image(conn, product_id, new_cover_url)
    logger.info(
        "Updated product #%s cover -> %s (saved %s)",
        product_id,
        new_cover_url,
        target_path,
    )
    return "updated"


def configure_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Generate product cover images via Ali DashScope (Qianwen).",
    )
    parser.add_argument("--db", type=Path, default=DEFAULT_DB_PATH, help="SQLite 数据库文件路径")
    parser.add_argument(
        "--cover-dir",
        type=Path,
        default=DEFAULT_COVER_DIR,
        help="生成封面在本地保存的目录（默认 static/productCover）",
    )
    parser.add_argument(
        "--base-url",
        default=DEFAULT_BASE_URL,
        help="对外访问封面的 HTTP 前缀，以 / 结尾",
    )
    parser.add_argument(
        "--api-key",
        default=os.environ.get("ALIYUN_QIANWEN_API_KEY") or os.environ.get("DASHSCOPE_API_KEY"),
        help="DashScope API-KEY（也可从环境变量 ALIYUN_QIANWEN_API_KEY 设置）",
    )
    parser.add_argument(
        "--model",
        default="wanx-v1",
        help="生成模型（如 wanx-v1 / wanx-style-v1）",
    )
    parser.add_argument(
        "--size",
        default="1280*720",
        help="图片尺寸，DashScope 当前仅允许 1024*1024/720*1280/1280*720/768*1152",
    )
    parser.add_argument(
        "--style",
        default="<photography>",
        help="风格，DashScope 需要传入如 <photography>/<anime> 这样的值",
    )
    parser.add_argument(
        "--poll-interval",
        type=int,
        default=8,
        help="异步任务轮询间隔 (秒)",
    )
    parser.add_argument(
        "--poll-timeout",
        type=int,
        default=480,
        help="异步任务最长等待时长 (秒)",
    )
    parser.add_argument(
        "--mode",
        choices=["full", "test"],
        default="full",
        help="full: 批量生成所有商品；test: 仅生成一条记录验证流程",
    )
    parser.add_argument("--product-id", type=int, help="只处理指定 product id")
    parser.add_argument(
        "--limit",
        type=int,
        help="限制处理的记录数（full 模式可用于分批）",
    )
    parser.add_argument(
        "--skip-existing",
        action="store_true",
        help="若封面已指向目标域名且文件存在，则跳过",
    )
    parser.add_argument(
        "--overwrite-files",
        action="store_true",
        help="若目标文件存在则覆盖，否则自动追加 _1、_2 后缀",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="仅打印将要执行的操作，不真正调用 API / 写入",
    )
    parser.add_argument(
        "--log-level",
        default="INFO",
        help="日志级别 (DEBUG, INFO, WARNING...)",
    )
    return parser


def validate_args(args: argparse.Namespace) -> None:
    if not args.api_key and not args.dry_run:
        raise SystemExit("缺少 DashScope API KEY，请通过 --api-key 或设置 ALIYUN_QIANWEN_API_KEY 环境变量。")
    if args.base_url and not args.base_url.endswith("/"):
        args.base_url += "/"
    if args.mode == "test" and args.limit is None and args.product_id is None:
        args.limit = 1
    args.style = normalize_style(args.style)
    if args.size not in DASHSCOPE_ALLOWED_SIZES:
        raise SystemExit(
            f"当前模型仅支持尺寸 {sorted(DASHSCOPE_ALLOWED_SIZES)}，请通过 --size 选择其中之一。",
        )


def raise_for_status_verbose(resp) -> None:
    try:
        resp.raise_for_status()
    except HTTPError as exc:  # noqa: PERF203
        text = resp.text or ""
        if len(text) > 2000:
            text = text[:2000] + "...<truncated>"
        raise HTTPError(f"{exc}. body={text}", response=resp) from exc


def main() -> None:
    parser = configure_parser()
    args = parser.parse_args()
    logging.basicConfig(
        level=getattr(logging, args.log_level.upper(), logging.INFO),
        format="%(asctime)s %(levelname)s %(message)s",
    )
    validate_args(args)

    ensure_directory(args.cover_dir)
    if args.db.suffix == ".sql":
        logger.warning(
            "检测到 .sql 文件：%s。请先导入为 SQLite 数据库，再传入 .db 文件路径。",
            args.db,
        )

    conn = sqlite3.connect(args.db)
    conn.row_factory = dict_factory

    try:
        rows = fetch_product_rows(
            conn,
            product_id=args.product_id,
            limit=args.limit,
        )
        if not rows:
            logger.warning("未找到需要处理的商品记录。")
            return
        session = requests.Session()
        try:
            summary = {"updated": 0, "skipped": 0, "failed": 0}
            total = len(rows)
            progress = ProgressBar(total)
            for idx, row in enumerate(rows, start=1):
                result = process_product(
                    session,
                    conn,
                    row,
                    api_key=args.api_key,
                    args=args,
                )
                summary[result] = summary.get(result, 0) + 1
                progress.update(idx)
            logger.info(
                "任务结束：更新=%s, 跳过=%s, 失败=%s",
                summary.get("updated", 0),
                summary.get("skipped", 0),
                summary.get("failed", 0),
            )
        finally:
            session.close()
    finally:
        conn.close()


if __name__ == "__main__":
    main()
