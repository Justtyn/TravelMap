#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
使用 DeepSeek API 扩充 TravelMap 商品与景点描述。
================================================
示例：
    python util/enrich_descriptions.py --table scenic --limit 5
"""

import argparse
import logging
import os
import re
import sqlite3
import string
import sys
import time
from typing import List, Optional, Sequence

import requests
from requests import RequestException


PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DB_PATH = os.path.join(PROJECT_ROOT, "db", "TravelMap.db")

DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions"
DEEPSEEK_API_KEY = "sk-e2d70fcc72474681a0f637252f65060a"
DEEPSEEK_MODEL = "deepseek-chat"

# 全局系统指令：保持 TravelMap App 文风，且事实准确
SYSTEM_PROMPT = (
    "你是 TravelMap App 的资深内容策划，负责把现有的景点和衍生商品介绍改写得更生动、可信。"
    "必须保留原始事实、数据和地理信息；语言自然，不堆叠形容词，也不胡编乱造。"
    "只输出一段连贯、流畅的中文描述，不要分风格（如文旅/商业/技术），不要加标题、标签、括号说明或列表。"
    "不要输出【】、***、--- 等分隔符，也不要提‘风格’‘版本’等字眼。"
    "最终文本将直接存入数据库并展示给用户，请确保其可读性和实用性。"
)

SCENIC_PROMPT_TEMPLATE = """
帮我扩写以下景点描述，要求：
1. 不改变核心事实，但更生动有画面感
2. 自然流畅，不堆叠形容词、符合中文表达习惯、不胡编乱造、不过度修饰、不使用夸张语气、要变现的专业
3. 适合用于 TravelMap App 的景点介绍
4. 仅输出一段连贯的中文长描述，不要分风格、不要标题、不要括号说明、不要技术参数
5. 不要包含【文旅宣传风格】等标签，也不要分段落或使用项目符号

景点名称：{name}
原始描述：{description}
"""

PRODUCT_PROMPT_TEMPLATE = """
帮我扩写以下商品描述，要求：
1. 不改变商品与所属景点的核心信息
2. 适度强调使用场景或体验价值
3. 适合用于 TravelMap App 商城推荐文案，语言自然不过度营销
4. 仅输出一段连贯的中文长描述，不要分风格、不要标题、不要括号说明
5. 不要包含【商业化风格】等标签，也不要分段落或使用项目符号

商品名称：{product_name}
所属景点：{scenic_name}
原始描述：{description}
"""

ASCII_ALNUM = set(string.ascii_letters + string.digits)


def _needs_ascii_gap(prev_text: str, next_text: str) -> bool:
    if not prev_text or not next_text:
        return False
    return prev_text[-1] in ASCII_ALNUM and next_text[0] in ASCII_ALNUM


def format_description(text: str, keep_newlines: bool) -> str:
    cleaned_lines = [line.strip() for line in text.strip().splitlines() if line.strip()]
    if not cleaned_lines:
        return ""

    if keep_newlines:
        return "\n".join(cleaned_lines)

    parts = [cleaned_lines[0]]
    for line in cleaned_lines[1:]:
        if _needs_ascii_gap(parts[-1], line):
            parts.append(" ")
        parts.append(line)

    compact = "".join(parts)
    compact = re.sub(r"[ \t]{2,}", " ", compact)
    return compact.strip()

# 在 format_description 后，或 update 前，加一个清理函数
def remove_style_headers(text: str) -> str:
    lines = text.splitlines()
    # 跳过以【xxx风格】开头的行
    filtered = []
    for line in lines:
        if re.match(r"^\s*【.*风格】\s*$", line):
            continue
        filtered.append(line)
    return "\n".join(filtered).strip()

class DeepSeekClient:
    def __init__(self, api_key: str, timeout: float = 30.0, model: str = DEEPSEEK_MODEL):
        self.api_key = api_key
        self.timeout = timeout
        self.model = model

    def complete(self, user_prompt: str, max_retries: int = 2) -> str:
        payload = {
            "model": self.model,
            "messages": [
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": user_prompt},
            ],
            "temperature": 0.4,
        }
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
        }

        last_error: Optional[str] = None
        for attempt in range(max_retries + 1):
            try:
                response = requests.post(
                    DEEPSEEK_API_URL,
                    json=payload,
                    headers=headers,
                    timeout=self.timeout,
                )
                response.raise_for_status()
                data = response.json()
                content = data["choices"][0]["message"]["content"].strip()
                if not content:
                    raise ValueError("response content is empty")
                return content
            except (RequestException, ValueError, KeyError) as exc:
                last_error = str(exc)
                if attempt == max_retries:
                    raise RuntimeError(f"DeepSeek 调用失败：{last_error}") from exc
                time.sleep(2 ** attempt)
        raise RuntimeError(f"DeepSeek 调用失败：{last_error}")


def get_logger(verbose: bool) -> logging.Logger:
    level = logging.DEBUG if verbose else logging.INFO
    logging.basicConfig(
        level=level,
        format="%(asctime)s [%(levelname)s] %(message)s",
        datefmt="%H:%M:%S",
    )
    return logging.getLogger("enrich")


def fetch_scenic_records(
    conn: sqlite3.Connection,
    include_empty: bool,
    limit: Optional[int],
) -> List[sqlite3.Row]:
    conditions: List[str] = []
    if not include_empty:
        conditions.append("description IS NOT NULL AND TRIM(description) <> ''")

    sql = "SELECT id, name, COALESCE(description, '') AS description FROM scenic"
    params: List[object] = []
    if conditions:
        sql += " WHERE " + " AND ".join(conditions)
    sql += " ORDER BY id"
    if limit:
        sql += " LIMIT ?"
        params.append(limit)

    cursor = conn.execute(sql, params)
    return cursor.fetchall()


def fetch_product_records(
    conn: sqlite3.Connection,
    include_empty: bool,
    limit: Optional[int],
) -> List[sqlite3.Row]:
    conditions: List[str] = []
    if not include_empty:
        conditions.append("p.description IS NOT NULL AND TRIM(p.description) <> ''")

    sql = """
        SELECT
            p.id,
            p.name,
            COALESCE(p.description, '') AS description,
            COALESCE(s.name, '未知景点') AS scenic_name
        FROM product AS p
        LEFT JOIN scenic AS s ON s.id = p.scenic_id
    """
    params: List[object] = []
    if conditions:
        sql += " WHERE " + " AND ".join(conditions)
    sql += " ORDER BY p.id"
    if limit:
        sql += " LIMIT ?"
        params.append(limit)

    cursor = conn.execute(sql, params)
    return cursor.fetchall()


def build_prompt(table: str, row: sqlite3.Row) -> str:
    if table == "scenic":
        description = row["description"] or "暂无原始描述，仅可根据名称补全。"
        return SCENIC_PROMPT_TEMPLATE.format(
            name=row["name"],
            description=description,
        )

    if table == "product":
        description = row["description"] or "暂无原始描述，请结合名称诚实补充。"
        scenic_name = row["scenic_name"] or "通用场景"
        return PRODUCT_PROMPT_TEMPLATE.format(
            product_name=row["name"],
            scenic_name=scenic_name,
            description=description,
        )

    raise ValueError(f"未知的表：{table}")


def update_description(
    conn: sqlite3.Connection,
    table: str,
    record_id: int,
    new_text: str,
    dry_run: bool,
) -> None:
    if dry_run:
        return

    if table not in {"scenic", "product"}:
        raise ValueError(f"非法的表名：{table}")

    conn.execute(
        f"UPDATE {table} SET description = ? WHERE id = ?",
        (new_text, record_id),
    )
    conn.commit()


def process_table(
    conn: sqlite3.Connection,
    table: str,
    rows: Sequence[sqlite3.Row],
    client: DeepSeekClient,
    dry_run: bool,
    show_text: bool,
    keep_newlines: bool,
    delay: float,
    max_retries: int,
    logger: logging.Logger,
) -> None:
    if not rows:
        logger.info("表 %s 没有待处理的记录。", table)
        return

    logger.info("开始处理表 %s，共 %d 条记录。", table, len(rows))
    for index, row in enumerate(rows, start=1):
        prompt = build_prompt(table, row)
        logger.debug("生成 prompt：%s", prompt)

        try:
            enriched = client.complete(prompt, max_retries=max_retries).strip()
        except RuntimeError as exc:
            logger.error("记录 %s#%s 生成失败：%s", table, row["id"], exc)
            continue

        formatted = format_description(enriched, keep_newlines=keep_newlines)
        formatted = remove_style_headers(formatted)  # 新增

        logger.info(
            "[%s] #%s (%d/%d) 更新完成，长度 %d 字符。",
            table,
            row["id"],
            index,
            len(rows),
            len(formatted),
        )
        logger.debug("原始描述：%s", enriched)
        logger.debug("格式化后：%s", formatted)
        if dry_run or show_text:
            logger.info("新描述内容：\n%s", formatted)

        update_description(conn, table, row["id"], formatted, dry_run=dry_run)

        if delay > 0 and index < len(rows):
            time.sleep(delay)


def parse_args(argv: Optional[Sequence[str]] = None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="调用 DeepSeek 扩充 scenic/product 描述。",
    )
    parser.add_argument(
        "--table",
        choices=["scenic", "product", "all"],
        default="all",
        help="限定处理的表，默认同时处理 scenic 与 product。",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=None,
        help="限制每个表最多处理多少记录，用于小批量验证。",
    )
    parser.add_argument(
        "--include-empty",
        action="store_true",
        help="包含 description 为空的记录，会根据名称尝试生成。",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="仅打印日志，不写入数据库。",
    )
    parser.add_argument(
        "--show-text",
        action="store_true",
        help="即使不是 dry-run 也直接打印生成的描述全文。",
    )
    parser.add_argument(
        "--keep-newlines",
        action="store_true",
        help="保留模型输出中的换行，不做单行压缩。",
    )
    parser.add_argument(
        "--delay",
        type=float,
        default=1.5,
        help="两次请求之间的延迟（秒），默认 1.5。",
    )
    parser.add_argument(
        "--max-retries",
        type=int,
        default=2,
        help="DeepSeek 调用失败时的重试次数。",
    )
    parser.add_argument(
        "--timeout",
        type=float,
        default=30.0,
        help="HTTP 请求超时时间，默认 30 秒。",
    )
    parser.add_argument(
        "--verbose",
        action="store_true",
        help="输出调试日志。",
    )
    return parser.parse_args(argv)


def main(argv: Optional[Sequence[str]] = None) -> int:
    args = parse_args(argv)
    logger = get_logger(args.verbose)

    if not os.path.exists(DB_PATH):
        logger.error("数据库不存在：%s", DB_PATH)
        return 1

    client = DeepSeekClient(
        api_key=DEEPSEEK_API_KEY,
        timeout=args.timeout,
    )

    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row

    tables: List[str]
    if args.table == "all":
        tables = ["scenic", "product"]
    else:
        tables = [args.table]

    try:
        if "scenic" in tables:
            scenic_rows = fetch_scenic_records(
                conn,
                include_empty=args.include_empty,
                limit=args.limit,
            )
            process_table(
                conn,
                "scenic",
                scenic_rows,
                client,
                dry_run=args.dry_run,
                show_text=args.show_text,
                keep_newlines=args.keep_newlines,
                delay=args.delay,
                max_retries=args.max_retries,
                logger=logger,
            )

        if "product" in tables:
            product_rows = fetch_product_records(
                conn,
                include_empty=args.include_empty,
                limit=args.limit,
            )
            process_table(
                conn,
                "product",
                product_rows,
                client,
                dry_run=args.dry_run,
                show_text=args.show_text,
                keep_newlines=args.keep_newlines,
                delay=args.delay,
                max_retries=args.max_retries,
                logger=logger,
            )
    finally:
        conn.close()

    logger.info("全部处理完成。")
    return 0


if __name__ == "__main__":
    sys.exit(main())
