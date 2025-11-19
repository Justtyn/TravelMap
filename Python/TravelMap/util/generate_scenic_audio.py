#!/usr/bin/env python3
"""
从 TravelMap-back.db 的 scenic.description 字段生成语音文件并更新 audio_url。

功能特性：
1. 自动清理文本（去掉空格和换行，仅保留文字与标点）。
2. 调用 DashScope Qwen3-TTS 接口生成音频，下载到 audio 目录，并写回数据库。
3. 内置进度条与统计信息，支持跳过已有音频、限制处理数量、Dry Run 测试等多种选项。
"""
from __future__ import annotations

import argparse
import json
import os
import re
import sqlite3
import sys
import tempfile
import time
import wave
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, Iterable, List, Optional, Tuple

import requests

try:
    import dashscope
except ImportError as exc:  # pragma: no cover - import error is fatal at runtime
    raise SystemExit(
        "未安装 dashscope，请先运行 `pip install \"dashscope>=1.24.6\"` 再执行本脚本。"
    ) from exc


DEFAULT_CDN_PREFIX = "https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/audio"
INVALID_FILENAME_CHARS = r"""<>:"/\\|?*"""


@dataclass
class ScenicRecord:
    scenic_id: int
    name: str
    description: str
    audio_url: Optional[str]


class ProgressBar:
    """简单终端进度条，避免额外依赖。"""

    def __init__(self, total: int) -> None:
        self.total = max(total, 1)
        self.current = 0

    def update(self, hint: str = "") -> None:
        self.current += 1
        filled = int(30 * self.current / self.total)
        bar = "#" * filled + "-" * (30 - filled)
        percent = self.current / self.total * 100
        message = f"\r[{bar}] {self.current:>3}/{self.total:<3} {percent:5.1f}% {hint:<40}"
        print(message, end="", flush=True)
        if self.current >= self.total:
            print()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="批量将 scenic.description 转换为语音并写入 audio_url。",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter,
    )
    parser.add_argument(
        "--db-path",
        default="db/TravelMap.db",
        help="SQLite 数据库路径",
    )
    parser.add_argument(
        "--output-dir",
        default="audio",
        help="音频输出目录，相对/绝对路径均可",
    )
    parser.add_argument(
        "--voice",
        default="Cherry",
        help="DashScope 音色，例如 Cherry、Ethan 等",
    )
    parser.add_argument(
        "--language",
        default="Chinese",
        help="语种参数，对应 DashScope language_type",
    )
    parser.add_argument(
        "--model",
        default="qwen3-tts-flash",
        help="DashScope 模型名称",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=None,
        help="只处理前 N 条记录，便于测试",
    )
    parser.add_argument(
        "--skip-existing",
        action="store_true",
        help="若 scenic.audio_url 已存在则跳过该条，避免重复生成",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="测试模式：只打印将要发送的文本和文件名，不调用 API、不写文件、不更新数据库",
    )
    parser.add_argument(
        "--api-base",
        default="https://dashscope.aliyuncs.com/api/v1",
        help="DashScope API Base，如使用新加坡地域请修改为 https://dashscope-intl.aliyuncs.com/api/v1",
    )
    parser.add_argument(
        "--max-chars",
        type=int,
        default=600,
        help="单次请求允许的最大字符数（遵循 Qwen3-TTS 计费标准）",
    )
    return parser.parse_args()


def ensure_api_key() -> str:
    api_key = os.getenv("DASHSCOPE_API_KEY")
    if not api_key:
        raise SystemExit(
            "未检测到 DASHSCOPE_API_KEY 环境变量，请先在 shell 中执行 "
            "`export DASHSCOPE_API_KEY=sk-xxx` 后再运行。"
        )
    return api_key


def detect_description_column(cursor: sqlite3.Cursor) -> str:
    columns = {row[1].lower(): row[1] for row in cursor.execute("PRAGMA table_info('scenic')")}
    for candidate in ("description", "desc"):
        if candidate in columns:
            return columns[candidate]
    raise SystemExit("scenic 表中未找到 description/desc 字段，请确认数据库结构。")


def load_scenic_records(
    conn: sqlite3.Connection,
    description_field: str,
    limit: Optional[int],
) -> List[ScenicRecord]:
    sql = (
        f"SELECT id, name, {description_field}, audio_url "
        "FROM scenic ORDER BY id"
    )
    params: Tuple[int, ...] = ()
    if limit is not None:
        sql += " LIMIT ?"
        params = (limit,)

    rows = conn.execute(sql, params).fetchall()
    records: List[ScenicRecord] = []
    for row in rows:
        record = ScenicRecord(
            scenic_id=row[0],
            name=row[1] or f"scenic_{row[0]}",
            description=row[2] or "",
            audio_url=row[3],
        )
        records.append(record)
    return records


def clean_text(text: str) -> str:
    """移除所有空白字符，仅保留文字与标点。"""
    return re.sub(r"\s+", "", text.strip())


def count_dashscope_chars(text: str) -> int:
    total = 0
    for ch in text:
        total += 1 if ch.isascii() else 2
    return total


def split_text_by_limit(text: str, max_chars: int) -> List[str]:
    """按照 DashScope 计费逻辑切分文本，确保每块不超过 max_chars。"""
    if max_chars <= 0:
        raise ValueError("max_chars 必须为正数")

    chunks: List[str] = []
    current: List[str] = []
    current_count = 0

    for ch in text:
        ch_len = 1 if ch.isascii() else 2
        if current and current_count + ch_len > max_chars:
            chunks.append("".join(current))
            current = [ch]
            current_count = ch_len
        else:
            current.append(ch)
            current_count += ch_len

    if current:
        chunks.append("".join(current))

    return chunks or [""]


def ensure_audio_path(base_path: Path, audio_format: str) -> Path:
    fmt = (audio_format or "wav").lower()
    expected_suffix = f".{fmt}"
    if base_path.suffix.lower() != expected_suffix:
        base_path = base_path.with_suffix(expected_suffix)
    return base_path


def build_cdn_url(file_path: Path) -> str:
    return f"{DEFAULT_CDN_PREFIX}/{file_path.name}"


def merge_wav_files(chunk_paths: List[Path], target_path: Path) -> None:
    params = None
    core_signature: Optional[Tuple[int, int, int, str]] = None
    with wave.open(str(target_path), "wb") as out_wav:
        for chunk_path in chunk_paths:
            with wave.open(str(chunk_path), "rb") as chunk_wav:
                chunk_params = chunk_wav.getparams()
                if params is None:
                    params = chunk_params
                    out_wav.setparams(chunk_params)
                    core_signature = (
                        chunk_params.nchannels,
                        chunk_params.sampwidth,
                        chunk_params.framerate,
                        chunk_params.comptype,
                    )
                else:
                    new_signature = (
                        chunk_params.nchannels,
                        chunk_params.sampwidth,
                        chunk_params.framerate,
                        chunk_params.comptype,
                    )
                    if new_signature != core_signature:
                        raise ValueError("分段 WAV 参数不一致，无法拼接。")
                out_wav.writeframes(chunk_wav.readframes(chunk_wav.getnframes()))


def merge_pcm_files(chunk_paths: List[Path], target_path: Path) -> None:
    with target_path.open("wb") as merged:
        for chunk_path in chunk_paths:
            merged.write(chunk_path.read_bytes())


def merge_audio_chunks(chunk_paths: List[Path], target_path: Path, audio_format: str) -> None:
    fmt = (audio_format or "wav").lower()
    if fmt == "wav":
        merge_wav_files(chunk_paths, target_path)
    elif fmt == "pcm":
        merge_pcm_files(chunk_paths, target_path)
    else:
        raise ValueError(f"暂不支持拼接 {fmt} 音频格式，请改用 wav/pcm。")


def safe_filename(name: str, extension: str) -> str:
    sanitized = name.strip()
    for char in INVALID_FILENAME_CHARS:
        sanitized = sanitized.replace(char, "")
    sanitized = re.sub(r"\s+", "_", sanitized)
    sanitized = sanitized or "scenic_audio"
    return f"{sanitized}.{extension}"


def request_tts_audio(
    *,
    text: str,
    api_key: str,
    model: str,
    voice: str,
    language: str,
) -> Dict[str, str]:
    response = dashscope.MultiModalConversation.call(
        model=model,
        api_key=api_key,
        text=text,
        voice=voice,
        language_type=language,
        stream=False,
    )
    response_dict = response if isinstance(response, dict) else getattr(response, "__dict__", {})
    output = response_dict.get("output")
    if output is None and hasattr(response, "output"):
        output = response.output
    if output is None:
        raise RuntimeError(f"DashScope 返回体缺少 output 字段：{response}")

    audio = output.get("audio") if isinstance(output, dict) else getattr(output, "audio", None)
    if audio is None:
        raise RuntimeError(f"DashScope output 缺少 audio 字段：{output}")

    if isinstance(audio, dict):
        audio_url = audio.get("url")
        audio_format = audio.get("format") or "wav"
    else:
        audio_url = getattr(audio, "url", None)
        audio_format = getattr(audio, "format", "wav")

    if not audio_url:
        raise RuntimeError(f"DashScope audio 缺少 url：{audio}")

    return {"url": audio_url, "format": audio_format or "wav"}


def download_audio_file(url: str, target_path: Path, retries: int = 3) -> None:
    last_error: Optional[Exception] = None
    for attempt in range(1, max(retries, 1) + 1):
        try:
            response = requests.get(url, timeout=120)
            response.raise_for_status()
            target_path.write_bytes(response.content)
            return
        except Exception as exc:  # noqa: BLE001 - 网络异常重试
            last_error = exc
            if attempt >= retries:
                break
            time.sleep(min(2 * attempt, 5))
    raise RuntimeError(f"下载音频失败（尝试 {retries} 次）：{last_error}")


def update_audio_url(
    conn: sqlite3.Connection,
    scenic_id: int,
    cdn_url: str,
) -> None:
    conn.execute("UPDATE scenic SET audio_url = ? WHERE id = ?", (cdn_url, scenic_id))


def process_records(
    records: Iterable[ScenicRecord],
    *,
    conn: sqlite3.Connection,
    args: argparse.Namespace,
) -> Tuple[int, int, List[str]]:
    api_key = ensure_api_key()
    dashscope.base_http_api_url = args.api_base
    output_dir = Path(args.output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    progress = ProgressBar(total=len(records))
    success = 0
    skipped = 0
    errors: List[str] = []

    for record in records:
        hint = f"ID {record.scenic_id} - {record.name}"
        try:
            if not record.description:
                skipped += 1
                progress.update(f"{hint}（无描述跳过）")
                continue
            if args.skip_existing and record.audio_url:
                skipped += 1
                progress.update(f"{hint}（已有音频）")
                continue

            cleaned_text = clean_text(record.description)
            char_count = count_dashscope_chars(cleaned_text)
            if char_count == 0:
                skipped += 1
                progress.update(f"{hint}（清理后为空）")
                continue
            text_chunks = split_text_by_limit(cleaned_text, args.max_chars)
            chunk_count = len(text_chunks)

            filename = safe_filename(record.name, "wav")
            base_path = output_dir / filename

            if args.dry_run:
                print(
                    f"[DryRun] Would process scenic_id={record.scenic_id}, "
                    f"chars={char_count}, chunks={chunk_count}, file={base_path}"
                )
                skipped += 1
                progress.update(f"{hint}（DryRun）")
                continue

            local_path = base_path
            cdn_url = build_cdn_url(local_path)
            audio_format: Optional[str] = None

            if chunk_count == 1:
                audio_meta = request_tts_audio(
                    text=text_chunks[0],
                    api_key=api_key,
                    model=args.model,
                    voice=args.voice,
                    language=args.language,
                )
                audio_format = (audio_meta.get("format", "wav") or "wav").lower()
                if audio_format not in ("wav", "mp3", "pcm"):
                    audio_format = "wav"
                local_path = ensure_audio_path(base_path, audio_format)
                cdn_url = build_cdn_url(local_path)
                download_audio_file(audio_meta["url"], local_path)
            else:
                with tempfile.TemporaryDirectory(prefix="scenic_audio_", dir=str(output_dir)) as tmpdir:
                    tmpdir_path = Path(tmpdir)
                    chunk_paths: List[Path] = []
                    for idx, chunk_text in enumerate(text_chunks):
                        audio_meta = request_tts_audio(
                            text=chunk_text,
                            api_key=api_key,
                            model=args.model,
                            voice=args.voice,
                            language=args.language,
                        )
                        chunk_format = (audio_meta.get("format", "wav") or "wav").lower()
                        if chunk_format not in ("wav", "pcm"):
                            raise ValueError(
                                "分段拼接仅支持 wav/pcm，请调整模型或参数以获得可拼接格式。"
                            )
                        if audio_format is None:
                            audio_format = chunk_format
                        elif chunk_format != audio_format:
                            raise ValueError(
                                f"分段音频格式不一致：{audio_format} vs {chunk_format}"
                            )
                        chunk_path = tmpdir_path / f"chunk_{idx}.{chunk_format}"
                        download_audio_file(audio_meta["url"], chunk_path)
                        chunk_paths.append(chunk_path)

                    if audio_format is None:
                        raise RuntimeError("未能确定音频格式，无法拼接。")

                    local_path = ensure_audio_path(base_path, audio_format)
                    cdn_url = build_cdn_url(local_path)
                    merge_audio_chunks(chunk_paths, local_path, audio_format)
            update_audio_url(conn, record.scenic_id, cdn_url)
            conn.commit()
            success += 1
            progress.update(f"{hint}（完成）")
        except Exception as exc:  # noqa: BLE001 - 统一捕获打印
            conn.rollback()
            errors.append(f"{hint}: {exc}")
            progress.update(f"{hint}（失败）")

    return success, skipped, errors


def main() -> None:
    args = parse_args()
    db_path = Path(args.db_path)
    if not db_path.exists():
        raise SystemExit(f"数据库文件不存在：{db_path}")

    conn = sqlite3.connect(str(db_path))
    try:
        description_field = detect_description_column(conn.cursor())
        records = load_scenic_records(conn, description_field, args.limit)
        success, skipped, errors = process_records(records, conn=conn, args=args)
    finally:
        conn.close()

    summary = {
        "processed": len(records),
        "success": success,
        "skipped": skipped,
        "failed": len(errors),
    }
    print("\n执行结果：")
    print(json.dumps(summary, ensure_ascii=False, indent=2))

    if errors:
        print("\n失败详情：")
        for err in errors:
            print(f"- {err}")
        raise SystemExit("部分任务失败，请检查日志后重试。")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n已取消。")
        sys.exit(1)
