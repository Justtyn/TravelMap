#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Generate scenic cover images via Ali DashScope (Qianwen) and update the SQLite DB.

This script will:
1. Read scenic records from the configured SQLite database.
2. Call the DashScope image-generation API with a photo-realistic prompt that matches each scenic name.
3. Download the generated image into static/cover/{safe_scenic_name}.ext.
4. Update scenic.cover_image so it points to http://139.59.227.54:5001/static/cover/{file_name}.

Usage examples (run from the project root):
    python util/update_scenic_covers.py --mode test --scenic-id 1 --dashscope-key sk-xxx
    python util/update_scenic_covers.py --mode full --skip-existing

Key flags:
    --mode              test 只处理指定 scenic_id；full 遍历全部景点
    --scenic-id         test 模式下的景点 ID
    --dashscope-key     Ali DashScope API key（必填）
    --skip-existing     cover_image 非空的记录直接跳过
    --allow-overwrite   覆盖已存在的封面文件；默认追加 _1、_2
    --cover-dir         本地静态文件目录，默认 static/cover
    --base-url          更新到数据库时使用的 URL 前缀
    --dry-run-download  仅打印计划，不下载也不写库
"""

from __future__ import annotations

import argparse
import json
import logging
import mimetypes
import os
import re
import sqlite3
import sys
import time
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Tuple
from urllib.parse import urlparse

import requests
from requests import Session
from requests.exceptions import HTTPError

PROJECT_ROOT = Path(__file__).resolve().parent.parent
DEFAULT_DB_PATH = PROJECT_ROOT / "db" / "TravelMap.db"
DEFAULT_COVER_DIR = PROJECT_ROOT / "static" / "cover"
DEFAULT_BASE_URL = "http://139.59.227.54:5001/static/cover/"

DASHSCOPE_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text2image/image-synthesis"
DASHSCOPE_ALLOWED_SIZES = {"1024*1024", "720*1280", "1280*720", "768*1152"}
DASHSCOPE_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/{task_id}"

PROMPT_TEMPLATE = (
    "生成一张关于「{name}」的高清实拍风格展示图，画面呈现景点最具代表性的元素与氛围。"
    "使用写实摄影风格，清晰锐利，阳光自然，色彩真实，构图居中或三分法，画面干净无人物。"
    "适度强调建筑细节或自然景观纹理，使其具有旅行宣传片般的视觉效果。"
)
NEGATIVE_PROMPT = "低质量, 模糊, 漫画, 二次元, 水印, 文字, logo, AI 痕迹, 失真, 畸变, 画面脏乱, 人物"

logger = logging.getLogger("cover-updater")


class ScenicRecord(dict):
    """Type hint helper for rows returned from sqlite with row_factory=dict_factory."""

    id: int
    name: str
    cover_image: Optional[str]


def dict_factory(cursor, row):
    return {col[0]: row[idx] for idx, col in enumerate(cursor.description)}


def ensure_directory(path: Path) -> None:
    path.mkdir(parents=True, exist_ok=True)


def sanitize_filename(name: str, scenic_id: int) -> str:
    """
    Remove characters that cannot appear in file names but preserve Chinese text.
    """
    candidate = re.sub(r"[\\/:*?\"<>|#]+", "_", name.strip())
    candidate = re.sub(r"\s+", "_", candidate)
    candidate = candidate.strip("_")
    if not candidate:
        candidate = f"scenic_{scenic_id}"
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
    scenic_id: int,
    extension: str,
    cover_dir: Path,
    allow_overwrite: bool,
) -> Tuple[str, Path]:
    base = sanitize_filename(name, scenic_id)
    file_name = f"{base}{extension}"
    target_path = cover_dir / file_name
    suffix = 1
    while target_path.exists() and not allow_overwrite:
        file_name = f"{base}_{suffix}{extension}"
        target_path = cover_dir / file_name
        suffix += 1
    return file_name, target_path


def guess_extension(media_type: Optional[str], remote_url: str) -> str:
    if media_type:
        clean_type = media_type.split(";")[0].strip()
        ext = mimetypes.guess_extension(clean_type)
        if ext:
            return ext
    path = urlparse(remote_url).path
    ext = os.path.splitext(path)[1]
    if ext and len(ext) <= 6:
        return ext
    return ".png"


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


def download_image(session: Session, url: str, target_path: Path) -> None:
    with session.get(url, timeout=120, stream=True) as resp:
        raise_for_status_verbose(resp)
        with open(target_path, "wb") as fh:
            for chunk in resp.iter_content(chunk_size=8192):
                if chunk:
                    fh.write(chunk)


def update_cover_image(conn: sqlite3.Connection, scenic_id: int, new_url: str) -> None:
    cur = conn.cursor()
    cur.execute("UPDATE scenic SET cover_image = ? WHERE id = ?", (new_url, scenic_id))
    conn.commit()


def fetch_scenic_rows(
    conn: sqlite3.Connection,
    *,
    scenic_id: Optional[int],
    limit: Optional[int],
) -> List[ScenicRecord]:
    cur = conn.cursor()
    sql = "SELECT id, name, cover_image FROM scenic"
    params: List = []
    if scenic_id is not None:
        sql += " WHERE id = ?"
        params.append(scenic_id)
    sql += " ORDER BY id ASC"
    if limit is not None:
        sql += f" LIMIT {int(limit)}"
    cur.execute(sql, params)
    rows = cur.fetchall()
    return rows


def should_skip_row(
    row: ScenicRecord,
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
                "Skipping scenic id=%s name=%s (already has cover %s)",
                row["id"],
                row["name"],
                existing_url,
            )
            return True
    return False


def process_scenic(
    session: Session,
    conn: sqlite3.Connection,
    row: ScenicRecord,
    *,
    api_key: str,
    args: argparse.Namespace,
) -> str:
    scenic_id = row["id"]
    scenic_name = row["name"]
    if should_skip_row(
        row,
        base_url=args.base_url,
        cover_dir=args.cover_dir,
        skip_existing=args.skip_existing,
    ):
        return "skipped"

    prompt = PROMPT_TEMPLATE.format(name=scenic_name)
    logger.info("Requesting new cover for scenic #%s «%s»", scenic_id, scenic_name)

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
        logger.exception("DashScope 请求失败 scenic=%s reason=%s", scenic_id, exc)
        return "failed"

    remote_url = result.get("url")
    media_type = result.get("media_type")
    if not remote_url:
        logger.error("任务成功但未返回 url scenic=%s payload=%s", scenic_id, result)
        return "failed"

    extension = guess_extension(media_type, remote_url)
    file_name, target_path = build_file_name(
        scenic_name,
        scenic_id,
        extension,
        args.cover_dir,
        args.overwrite_files,
    )
    new_cover_url = args.base_url.rstrip("/") + "/" + file_name

    try:
        download_image(session, remote_url, target_path)
    except Exception as exc:  # noqa: BLE001
        logger.exception("下载失败 scenic=%s url=%s reason=%s", scenic_id, remote_url, exc)
        return "failed"

    update_cover_image(conn, scenic_id, new_cover_url)
    logger.info(
        "Updated scenic #%s cover -> %s (saved %s)",
        scenic_id,
        new_cover_url,
        target_path,
    )
    return "updated"


def configure_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(
        description="Generate scenic cover images via Ali DashScope (Qianwen).",
    )
    parser.add_argument("--db", type=Path, default=DEFAULT_DB_PATH, help="SQLite 数据库文件路径")
    parser.add_argument(
        "--cover-dir",
        type=Path,
        default=DEFAULT_COVER_DIR,
        help="生成封面在本地保存的目录",
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
        help="full: 批量生成所有景点；test: 仅生成一条记录验证流程",
    )
    parser.add_argument("--scenic-id", type=int, help="只处理指定 scenic id")
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
    if args.mode == "test" and args.limit is None and args.scenic_id is None:
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
        rows = fetch_scenic_rows(
            conn,
            scenic_id=args.scenic_id,
            limit=args.limit,
        )
        if not rows:
            logger.warning("未找到需要处理的景点记录。")
            return
        session = requests.Session()
        try:
            summary = {"updated": 0, "skipped": 0, "failed": 0}
            total = len(rows)
            progress = ProgressBar(total)
            for idx, row in enumerate(rows, start=1):
                result = process_scenic(
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
