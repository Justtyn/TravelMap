"""Update scenic cover_image URLs by swapping the base prefix and URL-encoding the path.

Usage examples (run from the project root):
    python util/update_cover_prefix.py --dry-run
    python util/update_cover_prefix.py --table product --column cover_image --old-prefix ... --new-prefix ...

Key options:
    --db           SQLite 数据库路径（默认 db/TravelMap.db）
    --table        需要更新的表，默认 scenic，可切换为 product
    --column       目标列，默认 cover_image
    --old-prefix   需要替换掉的 URL 前缀
    --new-prefix   新的 URL 前缀，会自动去掉尾部的 /
    --dry-run      仅打印即将变更的 URL，不写入数据库
"""

from __future__ import annotations

import argparse
import sqlite3
from pathlib import Path
from typing import Iterable, Tuple
from urllib.parse import quote, unquote

PROJECT_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_DB_PATH = PROJECT_ROOT / "db" / "TravelMap.db"
OLD_PREFIX = "http://43.142.2.70:9090/uploads/cover"
NEW_PREFIX = "https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/travelmap-coverimage"


def encode_path_component(path: str) -> str:
    """Percent-encode every path segment while preserving slashes."""
    segments = path.split("/")
    encoded = [quote(unquote(segment), safe="~._-") for segment in segments]
    return "/".join(encoded)


def transform_url(url: str, old_prefix: str, new_prefix: str) -> str | None:
    if not url or not url.startswith(old_prefix):
        return None

    suffix = url[len(old_prefix) :]
    encoded_suffix = encode_path_component(suffix)
    normalized_new_prefix = new_prefix.rstrip("/")

    if encoded_suffix and not encoded_suffix.startswith("/"):
        encoded_suffix = "/" + encoded_suffix

    return normalized_new_prefix + encoded_suffix


def gather_updates(
    conn: sqlite3.Connection,
    table: str,
    column: str,
    old_prefix: str,
    new_prefix: str,
) -> Iterable[Tuple[str, int]]:
    sql = f"SELECT id, {column} AS value FROM {table} WHERE {column} LIKE ?"
    for row in conn.execute(sql, (f"{old_prefix}%",)):
        new_value = transform_url(row["value"], old_prefix, new_prefix)
        if new_value and new_value != row["value"]:
            yield new_value, row["id"]


def apply_updates(
    conn: sqlite3.Connection,
    table: str,
    column: str,
    updates: Iterable[Tuple[str, int]],
) -> int:
    sql = f"UPDATE {table} SET {column} = ? WHERE id = ?"
    with conn:
        result = conn.executemany(sql, list(updates))
    return result.rowcount if result else 0


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Replace scenic cover_image URL prefixes and encode filenames.",
    )
    parser.add_argument(
        "--db",
        default=str(DEFAULT_DB_PATH),
        help=f"Path to SQLite database (default: {DEFAULT_DB_PATH})",
    )
    parser.add_argument("--table", default="scenic")
    parser.add_argument("--column", default="cover_image")
    parser.add_argument("--old-prefix", default=OLD_PREFIX)
    parser.add_argument("--new-prefix", default=NEW_PREFIX)
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Only print the pending updates without writing to the database.",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    db_path = Path(args.db)
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row

    updates = list(
        gather_updates(
            conn=conn,
            table=args.table,
            column=args.column,
            old_prefix=args.old_prefix,
            new_prefix=args.new_prefix,
        ),
    )

    if not updates:
        print("No rows require updates.")
        return

    if args.dry_run:
        for new_value, row_id in updates:
            print(f"id={row_id}: {new_value}")
        print(f"{len(updates)} rows would be updated.")
        return

    updated = apply_updates(
        conn=conn,
        table=args.table,
        column=args.column,
        updates=updates,
    )
    print(f"Updated {updated} rows in {db_path}.")


if __name__ == "__main__":
    main()
