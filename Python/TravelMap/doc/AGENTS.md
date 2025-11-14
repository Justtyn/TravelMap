# Repository Guidelines

## Project Structure & Module Organization
- `app.py` is the single Flask entry point housing routes, DB helpers, and schema guards; keep new modules small and import them here via blueprints if you split functionality.
- `db/TravelMap.db` stores the working SQLite database; schema upgrades should be expressed as SQL migration snippets checked into `doc/` before touching the file.
- `doc/` contains reference API docs (`API.md`, `API_DOCUMENT.md`)—update the same section names when adding endpoints.
- `tests_smoke.py` exercises the REST surface through `app.test_client()`; add further scenarios beside the existing numbered blocks.
- Top-level assets such as `requirements.txt` and future scripts belong beside `app.py` to keep relative paths simple.

## Build, Test, and Development Commands
- `python3 -m venv .venv && source .venv/bin/activate` – create an isolated interpreter for local work.
- `pip install -r requirements.txt` – install Flask and Werkzeug versions pinned for this backend.
- `python app.py` – start the development server (defaults to `http://127.0.0.1:5000`); ensure `db/TravelMap.db` is writable so `ensure_schema()` can patch tables.
- `python tests_smoke.py` – run the end-to-end smoke flow that seeds scenic/product data, registers a user, and calls every public API.

## Coding Style & Naming Conventions
- Follow PEP 8: 4-space indentation, lowercase_with_underscores for functions/routes, and UPPER_SNAKE_CASE for constants such as `DB_PATH`.
- Keep handlers small and group helpers (JSON utilities, DB helpers) above the routes section as in `app.py`; prefer inline SQL with explicit column lists.
- Document non-obvious behavior in docstrings or short `#` comments placed above the affected block.

## Testing Guidelines
- Extend `tests_smoke.py` with deterministic fixtures—mirror the existing numbered sections (PING, REGISTER, etc.) so logs stay readable.
- Name helper methods `test_<module>_<behavior>` if you migrate to pytest; store shared data builders near the top of the file.
- Aim for coverage of new endpoints (happy path + error path) before opening a PR; ensure tests leave the database in a clean state or reset via seed helpers.

## Commit & Pull Request Guidelines
- Git history shows concise, descriptive summaries (e.g., `接口文档`, `后端搭建基本完成`); keep messages under 60 characters and prefer imperative moods.
- Reference linked issues or requirements in the PR description, outline schema changes, and include screenshots for API responses only when the payload structure changes.
- Every PR should list: purpose, main changes, testing evidence (`python tests_smoke.py` output snippet), and any DB migration steps reviewers must run.

## Security & Configuration Tips
- Never commit `.db` replacements without confirming migrations; back up `db/TravelMap.db` before destructive schema work.
- Validate all incoming JSON with explicit field checks (see `get_json()` usage) and avoid trusting client-provided IDs without verifying ownership in SQL.

## 特别要求（请严格遵守）
- 与用户互动时 **必须使用中文回复**，包含最终交付说明与讨论。
- 用户已永久移除冒烟测试脚本，后续处理任务 **禁止创建或运行任何测试**；如需验证逻辑，请通过代码审查、手工推演或用户指定方式。
- 当前阶段的工作聚焦前端官网与文档体验，**不得改动既有 API 接口逻辑或响应结构**（除非用户未来明确授权）。
