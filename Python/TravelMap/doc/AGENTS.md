# Repository Guidelines

## Project Structure & Module Organization
- `app.py` 为唯一的 Flask 入口，包含 API、数据库工具以及静态站点路由；若需扩展功能，请以蓝图或模块化方式拆分后再在此注册。
- `db/TravelMap.db` 持有真实业务数据（用户、景点、商品等），修改前请先导出备份；`doc/` 存放 Markdown 文档（API_DOC、AGENTS 等）。
- `templates/` 与 `static/` 驱动首页、文档、功能介绍等官网页面；修改样式先在本地预览再提交。
- 未启用专门的自动化测试目录，所有脚本和工具应放在仓库根目录或 doc/ 下，保持层级清晰。

## Build, Test & Development Commands
- `pip install -r requirements.txt`：安装 Flask、Werkzeug 及前端展示所需依赖。推荐在虚拟环境内执行。
- `python app.py`：启动 API 与官网；默认监听 `0.0.0.0:5000`（或 5001），包括 Markdown 在线预览与下载功能。
- `sqlite3 db/TravelMap.db`：快速查看或补充数据，建议配合 `.schema`、`.tables` 指令使用。

## Coding Style & Naming Conventions
- 代码遵循 PEP 8，使用 4 空格缩进；变量/函数使用 `snake_case`，常量（如 `DB_PATH`）使用 `UPPER_SNAKE_CASE`。
- Flask 路由名与模板文件保持语义一致，如 `/features` 对应 `features.html`。
- 文档采用 Markdown，一级标题 `#`，并保持英文文件名以便跨平台引用。

## Testing Guidelines
- 当前阶段**不运行或恢复任何自动化测试脚本**；如需验证行为，请通过手动调用 API 或阅读数据来完成。
- 若必须引入新的测试方式，需先与维护者确认，并在 PR 中说明验证步骤与影响面。

## Commit & Pull Request Guidelines
- 提交信息保持简洁、聚焦动机，如 “feat: add markdown viewer” 或 “chore: update docs download link”；避免一次提交兼做多项无关改动。
- PR 描述需包含：变更摘要、影响的模块/接口、验证方式（即便是手工验证也要说明）以及潜在风险。
- 若改动数据库结构或示例数据，务必说明迁移/回滚步骤，并附上必要的 SQL 片段。

## Security & Agent-Specific Notes
- 所有对外回复必须使用中文，避免混用其他语言；仅在代码或命令示例中使用英文。
- API 接口的响应结构已稳定，除非获得明确授权，否则禁止调整字段或语义。
- Markdown 文档可通过 `/docs/view/<文件>` 在线查看，若提供下载链接请加 `?download=1` 触发附件模式，防止浏览器直接渲染。
