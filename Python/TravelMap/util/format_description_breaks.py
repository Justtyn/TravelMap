#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
为数据库 description 字段添加句号换行格式化。

此工具遍历 scenic 和 product 表的 description 字段，
将所有句号（。）替换为句号加换行符（。\n），
以改善文本在 App 中的显示效果。

Usage examples:
    python util/format_description_breaks.py --dry-run
    python util/format_description_breaks.py --table scenic
    python util/format_description_breaks.py --table product
    python util/format_description_breaks.py
"""

from __future__ import annotations

import argparse
import sqlite3
from pathlib import Path
from typing import Iterable, Tuple

PROJECT_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_DB_PATH = PROJECT_ROOT / "db" / "TravelMap.db"


def add_line_break_after_period(text: str) -> str:
    """在每个句号后添加换行符，但保留原文本内容"""
    if not text:
        return text
    # 将句号替换为句号+换行符
    return text.replace('。', '。\n')


def gather_updates(
        conn: sqlite3.Connection,
        table: str,
) -> Iterable[Tuple[str, int]]:
    """收集需要更新的记录"""
    sql = f"SELECT id, description FROM {table} WHERE description IS NOT NULL AND TRIM(description) != ''"
    for row in conn.execute(sql):
        original_desc = row["description"]
        new_desc = add_line_break_after_period(original_desc)

        # 只处理内容确实发生变化的记录
        if new_desc != original_desc:
            yield new_desc, row["id"]


def apply_updates(
        conn: sqlite3.Connection,
        table: str,
        updates: Iterable[Tuple[str, int]],
) -> int:
    """应用更新到数据库"""
    sql = f"UPDATE {table} SET description = ? WHERE id = ?"
    with conn:
        result = conn.executemany(sql, list(updates))
    return result.rowcount if result else 0


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="为 scenic 和 product 表的 description 字段在句号后添加换行符",
    )
    parser.add_argument(
        "--db",
        default=str(DEFAULT_DB_PATH),
        help=f"Path to SQLite database (default: {DEFAULT_DB_PATH})",
    )
    parser.add_argument(
        "--table",
        choices=["scenic", "product", "all"],
        default="all",
        help="要处理的表，默认为 all（处理两个表）",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="仅打印即将变更的内容，不写入数据库",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    db_path = Path(args.db)

    if not db_path.exists():
        print(f"错误：数据库文件不存在: {db_path}")
        return

    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row

    # 确定要处理的表
    tables = [args.table] if args.table != "all" else ["scenic", "product"]

    total_updates = 0

    for table in tables:
        print(f"\n正在处理表: {table}")

        updates = list(gather_updates(conn=conn, table=table))

        if not updates:
            print(f"  表 {table} 中没有需要更新的记录")
            continue

        if args.dry_run:
            print(f"  将更新 {len(updates)} 条记录:")
            for new_value, row_id in updates[:5]:  # 只显示前5条作为示例
                original = conn.execute(f"SELECT description FROM {table} WHERE id = ?", (row_id,)).fetchone()[0]
                print(f"    id={row_id}:")
                print(f"      原文: {original[:50]}...")
                print(f"      新文: {new_value[:50]}...")
            if len(updates) > 5:
                print(f"    ... 还有 {len(updates) - 5} 条记录")
        else:
            updated = apply_updates(
                conn=conn,
                table=table,
                updates=updates,
            )
            print(f"  已更新表 {table} 中的 {updated} 条记录")
            total_updates += updated

    if not args.dry_run:
        print(f"\n总共更新了 {total_updates} 条记录")
        conn.close()
    else:
        print(f"\n预览模式结束，总共发现 {total_updates} 条记录需要更新")
        conn.close()


if __name__ == "__main__":
    main()