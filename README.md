# TravelMap

智慧文旅一体化解决方案，包含 Android 客户端、Flask 后端与同一套示例数据库。首个正式版 APK 已发布，线上后端部署在 `https://canulove.me` 并同步提供文档、API Explorer 与官方展示页。

## 🔗 快速导航
- 正式版 APK：`Python/TravelMap/static/TravleMap.apk`（线上可从 [canulove.me/static/TravleMap.apk](https://canulove.me/static/TravleMap.apk) 下载）
- 后端体验站：<https://canulove.me>
- API 文档：`Android/app/API_DOC.md`、`Python/TravelMap/doc/API_DOC.md` 或线上 `/docs`、`/api-explorer`

## 项目概览
- **定位**：面向高校课程设计/文旅行业路演的成套 Demo，覆盖景点导览、旅游商品交易、行程记录与用户成长任务链。
- **组成**：
  - `Android/`——原生 Android App，Material3 + RecyclerView Feed + AMap SDK。
  - `Python/TravelMap/`——Flask + SQLite REST 后端，含官网/文档静态页与可下载 APK。
  - `db/TravelMap.db`——示例库，预填用户、景点、商品、收藏、订单、行程等业务数据。
- **部署**：后端托管在 canulove.me，Android App 默认指向公网 API，可直接安装体验。

## 架构一览
| 层级 | 说明 |
| --- | --- |
| Android 客户端 | `MainActivity` 管理 Home / Mall / Booking / Plan / My 五大模块，`BaseFeedFragment` 统一搜索、骨架屏与下拉刷新；`TravelRepository`/`UserCenterRepository` 封装所有 API 调用。 |
| 后端服务 | 单文件 `app.py` 基于 Flask 3.0 + sqlite3，实现用户、景点、商品、收藏、去过、购物车、订单、行程计划接口以及 `/` 官网、`/docs` 文档中心。 |
| 静态资源 | `Python/TravelMap/static` 提供 APP 截图、Logo、Shimmer 风格的文案页与正式版 APK；`templates` 渲染首页、功能页、API Explorer。 |

## 核心功能
### Android App
- **灵感流与商城**：RecyclerView + Glide 呈现图文卡片，支持 Banner、搜索、下拉刷新与骨架屏。
- **景点详情**：展示城市、地址、经纬度、图文攻略，支持收藏、去过打卡（含评分弹窗）。
- **商品/预订链路**：门票、酒店、周边商品统一展示，加入购物车后可创建订单。
- **个人中心**：登录/注册/微信登录（Mock），收藏与去过列表，购物车、订单、个人资料编辑。
- **体验细节**：Material 3 主题、暗色模式适配、空态提示、`CircularProgressIndicator` 状态反馈。

### Flask 后端 & 官网
- **统一 API**：`/api/auth/* /api/scenics /api/products /api/cart /api/orders ...`，响应均为 `{code,msg,data}`。
- **数据透出**：`/api/favorites`、`/api/visited` 支持透出关联实体，方便 App 直接渲染。
- **官网/文档**：主页实时统计用户/景点/订单数量，FAQ、场景截图、分享链接齐全；`/api-explorer` 内置可交互的接口示例。
- **部署状态**：当前版本运行于 canulove.me，示例 APK 与文档同步托管。

## 目录速览
```
TravelMap
├─ Android/              # 安卓工程（Gradle、源码、API 说明）
├─ Python/TravelMap/     # Flask 后端、静态资源、文档、数据库
├─ LICENSE               # MIT License
└─ README.md             # 本文件
```

## 技术栈
- **客户端**：Android SDK 36、Kotlin/Java（参照源码）、Jetpack RecyclerView、SwipeRefreshLayout、Glide、AMap SDK、Facebook Shimmer。
- **后端**：Python 3.11+、Flask 3.0、Werkzeug、SQLite、Jinja2 模板、纯前端静态页面。
- **工具链**：Gradle 8.13、Android Gradle Plugin 8.13.1、`pip`、`venv`。

## 开发环境搭建
### 1. Android App
```bash
cd Android
./gradlew assembleDebug          # 生成 app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug           # 安装到连接的设备/模拟器
```
- 使用 Android Studio Hedgehog+，确保本地 JDK = 11，Android SDK = 33~36。
- 默认 `BuildConfig.API_BASE_URL = "http://138.68.59.41:5001"`，若要连本地后端请在 `app/build.gradle` 调整该字段或使用 `buildConfigField` 覆盖。

### 2. Flask 后端
```bash
cd Python/TravelMap
python3 -m venv .venv
source .venv/bin/activate  # Windows 使用 .venv\Scripts\activate
pip install -r requirements.txt
python app.py              # 默认 0.0.0.0:5001
```
- 依赖内置的 `db/TravelMap.db`，无需额外迁移；如需重建可参考 `db/main.sql`。
- 本地开发访问 `http://127.0.0.1:5001`，`/` 为官网、`/docs` 为文档、`/api/*` 为 REST 接口。

## 部署与运维
- **生产环境**：当前版本部署在 `https://canulove.me`，可由 `gunicorn/waitress + Nginx` 托管，也可直接 `python app.py` + 反向代理。
- **静态资源**：APK、截图等统一放在 `Python/TravelMap/static`，部署后可通过 `/static/...` 访问，官网会自动展示 APK 元数据（文件大小、SHA256、更新时间）。
- **健康检查**：`/ping` 返回 `{"code":200,"data":{"msg":"pong"}}`，适合作为负载均衡探针。

## API 文档与调试
- Markdown 版本：`Android/app/API_DOC.md`、`Python/TravelMap/doc/API_DOC.md`。
- 在线阅读：部署后访问 `https://canulove.me/docs`，或打开 `https://canulove.me/api-explorer` 测试接口。
- 所有接口均为 JSON 输入输出，字段命名遵循 `snake_case`，状态码统一见文档 0. 通用约定章节。

## 版本与发布
| 版本 | 日期 | 说明 |
| --- | --- | --- |
| v1.0 (首个正式版) | 2025-11 | 首发 APK、完整前后端联调、canulove.me 正式上线。 |
| 0.9.x Beta | 2025-10 | 内测阶段，完善商城、行程、文档及 APK 下载页。 |

正式版 APK 位于 `Python/TravelMap/static/TravleMap.apk`，也会在官网首页“立即下载”卡片中展示版本号、更新时间与 SHA256。

## 开发规范与文档
- Android 端开发约定详见 `Android/AGENTS.md`、`Android/DEV_LOG.md`。
- 后端模块说明、功能蓝图与课程材料位于 `Python/TravelMap/doc/*.md`（包含 DEV_LOG、软件说明书、API 列表）。
- 设计蓝图、课程策划在 `设计蓝图.md`，可作为答辩/文档素材。

## 许可证
本项目以 [MIT License](LICENSE) 开源，可自由复制、修改与分发。引用或二次开发时请保留原版权信息。
