# TravelMap 软件说明文档（2025-11-15 可投放版）

> 安卓开发组编写的最新版软件说明，可直接用于官网复制摘要或作为演示材料。若站点信息更新，请以本文件为准。

---

## 开发说明（Dev Log）

### 一、整体架构与基础设施
1. **登录 / 注册体系**
   - `LoginActivity` 调用 `AuthRepository` 完成账号登录，成功后通过 `UserPreferences` 缓存用户 JSON，并跳转 `MainActivity`。
   - `RegisterActivity` 对接注册接口，遵循 `app/API_DOC.md` 中的参数规范。注册成功后可直接使用刚创建的账号登录。
2. **用户会话管理**
   - `UserPreferences` / `UserProfile` 封装 SharedPreferences 读写，提供 `saveUser()`、`getUserProfile()`、`clear()` 等方法。
   - `MainActivity` 启动时校验登录态，若无有效用户信息则跳转 `LoginActivity`。
3. **导航与页面容器**
   - `MainActivity` 管理首页（Home）、商城（Mall）、预订（Booking）、行程（Plan，后续将替换）、我的（My）五个 Tab。
   - `BaseFeedFragment` 提供统一的搜索框、Banner、列表、下拉刷新、骨架屏逻辑，`HomeFragment` / `MallFragment` / `BookingFragment` 继承并实现数据加载。
4. **网络层**
   - `ApiClient` 封装 GET / POST / PUT / DELETE 请求，内置 Base URL、超时时间及 JSON 解析。
   - `TravelRepository` 负责景点、商品列表与详情；`UserCenterRepository` 负责收藏、去过、购物车、订单、个人资料等接口。

### 二、首页 / 商城 / 预订功能
1. **Feed 列表与搜索**
   - 使用 `FeedAdapter` 渲染 `item_feed_card`，展示封面、标题、描述、地址、经纬度、库存、访问时间、评分等字段。
   - 搜索框支持键盘 Search 与右侧图标触发，列表支持下拉刷新，骨架屏在初次加载时展示三张占位卡片。
2. **详情页跳转**
   - 点击卡片后自动根据 Fragment 类型跳转到 `ScenicDetailActivity`（首页）或 `ProductDetailActivity`（商城 / 预订），并传递 `EXTRA_SCENIC_ID` / `EXTRA_PRODUCT_ID`。
3. **Banner、空态、错误状态**
   - 各 Fragment 配置不同的 Banner 标题 / 副标题 / 图片；当接口返回空列表或异常时，展示定制化空态文字与 Toast。

### 三、景点 / 商品详情
1. **数据展示**
   - `ScenicDetailActivity`：展示名称、城市、地址、经纬度、描述，骨架屏加载，支持收藏与“去过”切换（含评分弹窗）；顶部 Toolbar 副标题显示“首页 / 景点详情”。
   - `ProductDetailActivity`：展示名称、类型（TICKET / TRAVEL / HOTEL 等）、价格、库存、地址、描述，骨架屏加载，支持收藏与加入购物车；若类型为 HOTEL，则 Toolbar 副标题显示“预订 / 商品详情”，否则显示“商城 / 商品详情”。后续需在经纬度下方加地图组件。
2. **交互**
   - 收藏 / 去过 / 加入购物车操作均调用 `UserCenterRepository` 对应接口，并在按钮右侧显示小型 `CircularProgressIndicator`。
   - “去过”支持 1~5 分评分及取消记录；收藏 / 去过状态变化后实时刷新按钮文案。
3. **地图与位置（待扩展）**
   - 目前仅展示经纬度文本，计划引入地图组件并在详情页中展示。

### 四、我的页面与个人中心
1. **MyFragment**
   - 显示头像、用户名、邮箱、快捷菜单（个人资料、收藏、去过、订单、购物车），骨架屏在首次进入时展示整体占位；“退出登录”按钮使用品牌蓝色背景，点击后清理登录态并跳回登录页。
2. **二级页面（均避让安全区、含骨架屏与面包屑副标题）**
   - `UserInfoActivity`：查看 / 编辑手机号、邮箱，Toolbar 副标题“我的 / 个人资料”。
   - `FavoritesActivity`：通过 `MaterialButtonToggleGroup` 切换“商品收藏 / 景点收藏”，顶部显示“我的 / 收藏”；列表骨架 + SwipeRefresh。
   - `VisitedActivity`：展示 `/api/visited` 返回的历史景点，点击可进入景点详情；Toolbar 副标题“我的 / 去过”。
   - `OrdersActivity`：展示 `/api/orders` 列表，下一步需支持订单详情与更多字段；Toolbar 副标题“我的 / 订单”。
   - `CartActivity`：展示购物车条目、下单按钮（调用 `/api/orders`），支持骨架屏；Toolbar 副标题“我的 / 购物车”。后续需支持删除 / 修改数量、丰富提交字段。
3. **收藏 / 去过页面点击行为**
   - 收藏页面根据当前 Tab 跳转到 Scenic / Product 详情；去过页面点击进入景点详情。

### 五、购物车与订单
1. **购物车**
   - `CartActivity` 使用 `CartAdapter` 渲染 `item_cart_entry`，展示商品封面、标题、描述、价格标签与数量。
   - 下单调用 `UserCenterRepository.createOrder()`，成功后跳转 `OrderSuccessActivity`（展示订单号、金额）。
   - 当前支持骨架屏加载与空态提示，后续需补充商品数量修改、删除、所有下单字段校验。
2. **订单管理**
   - `OrdersActivity` 列出所有订单（使用 FeedAdapter）；当前仅显示卡片信息，后续需补充订单详情页面。

### 六、UI / UX 统一
1. **骨架屏**
   - 详情页、首页 / 商城 / 预订列表、“我的”页、收藏 / 去过 / 订单 / 购物车等全部支持 Shimmer 骨架。
2. **安全区处理**
   - 所有详情页与“我的”二级页面使用 `android:fitsSystemWindows="true"`，Toolbar 自动避让状态栏。
3. **面包屑导航**
   - 各详情页与二级页面的 Toolbar 副标题统一展示当前层级路径，提升定位感。

### 后续重点（新要求）
1. **行程页 → 地图页**
   - 将现有 Plan 页替换为地图视图，展示数据库中所有景点的坐标及当前用户实时定位；废弃原行程接口与 UI。
2. **景点详情地图组件**
   - 在 `ScenicDetailActivity` 中的经纬度下方添加地图组件，直观呈现景点位置。
3. **购物车流程增强**
   - 下单时使用数据库中所有必要字段、支持删除商品与修改数量，流程提示更清晰。
4. **订单详情**
   - `OrdersActivity` 中的每条订单可点击查看详情：状态、收货人、联系方式、子项列表等。

> 以上开发说明涵盖当前所有已实现模块及 UI / 交互细节，后续迭代将按“行程页改版 → 景点详情地图 → 购物车增强 → 订单详情”顺序推进。

---

## 1. 软件概述（Software Overview）
- **软件名称**：TravelMap
- **软件定位**：集“目的地发现 + 地图导航 + 旅行电商”于一体的本地游伴侣 App
- **主要功能简介**：TravelMap 通过首页 Feed、商城 / 预订频道和地图页串联“灵感 → 决策 → 到店”的全流程，用户可浏览城市热门玩法、查看实时坐标、收藏心仪景点、直接下单门票或旅行商品，并用“我的”中心管理收藏、去过、购物车与订单。统一的骨架屏、搜索、面包屑导航与 Material 3 组件保证在浅色 / 深色模式以及不同尺寸设备上都有一致体验。
- **目标用户**：热衷周末出行、城市漫游与轻量化旅行计划的高校学生、背包客与本地新居民。
- **适用场景**：周末短途、毕业旅行、团建踩点、到店前核对门票 / 订单、分享打卡脚本等。
- **当前版本**：v1.0.0（versionCode 1，对应 `app/build.gradle` 中的 `versionName "1.0"`）
- **更新日期**：2025-11-15

## 2. 项目背景与设计理念（Background & Design Philosophy）
- **痛点 / 需求**：传统 OTA 对本地生活支持有限，地图 App 又缺乏场景化运营内容，导致“找灵感、看位置、买门票”需要在多个 App 间切换。
- **开发初衷**：打造一款讲得清的本科课程示范项目，同时可直接演示真实业务流程，方便答辩、展示和团队协作。
- **解决问题**：把“内容 + 地图 + 交易”整合在一个底部导航中，提供骨架屏、空态、加载态，确保弱网设备也能顺畅浏览；通过 `UserPreferences` 与 `UserProfile` 统一登录态管理，减少重复输入。
- **核心价值**：高一致性的 UI / UX、即时的地图定位、全链路的旅行商品下单体验，以及面向口头展示的中文注释与结构化代码。
- **技术 / 架构理念**：Activity + Fragment 容器搭配 Repository + 数据模型，网络层使用轻量 HttpURLConnection 包装；UI 贯彻 Material 3、骨架屏和 Safe Area 处理，并为地图与列表提供独立模块以便替换或扩展。

## 3. 软件功能介绍（Features）

### 3.1 核心功能
1. **智能推荐首页（Home）**
   - 功能说明：使用 `TravelRepository.fetchScenicFeed()` 拉取景点列表，`BaseFeedFragment` 自动提供搜索框、Banner、下拉刷新与骨架屏。
   - 用户流程：进入 App → 选择“首页” → 输入关键词或直接下拉刷新 → 点进卡片查看详情。
   - 使用示例：搜索「海岛」，立即获得带城市 / 评分 / 库存的卡片，继续点击跳入详情页。
2. **商城 / 预订频道**
   - 功能说明：`MallFragment` 与 `BookingFragment` 调用 `fetchProductsByTypes()` 将商品按类型拆分，所有卡片支持收藏与跳转商品详情。
   - 用户流程：切换到底部 Tab → 按关键词筛选 → 加入购物车或直接下单。
   - 使用示例：在预订页选择酒店类型商品，点击“加入购物车”后在购物车页面统一提交订单。
3. **景点 / 商品详情页**
   - 功能说明：`ScenicDetailActivity` / `ProductDetailActivity` 展示详情、地址 / 经纬度、库存 / 价格、收藏与“去过”按钮，并实时调用 `UserCenterRepository` 更新状态。
   - 用户流程：从任何 Feed 卡片进入 → 查看面包屑导航和骨架屏加载 → 执行收藏 / 评分 / 加入购物车 → 完成后返回列表自动刷新。
4. **地图与定位（Plan Tab）**
   - 功能说明：`MapFragment` 结合高德 3D 地图 SDK、`AMapLocationClient` 与 `TravelRepository.fetchScenicMapPoints()` 在地图中绘制所有景点并显示当前定位、缩放、Marker 缩略图。
   - 用户流程：首次进入提示定位权限 → MapView 自动聚焦至城市 → 点击任意 Marker 进入景点详情。
5. **订单与购物车闭环**
   - 功能说明：`CartActivity` / `OrdersActivity` 读取 `/api/cart` `/api/orders` 数据，提供骨架屏、空态、一次性下单与成功页；后续可扩展删除 / 修改数量。
   - 使用示例：挑选多个商品 → 统一在购物车提交 → 跳至订单成功页查看订单号，再到订单列表追踪。

### 3.2 次要功能
- **账号体系**：`LoginActivity`、`RegisterActivity`、`UserPreferences` 负责登录 / 注册 / 缓存；`MainActivity` 启动时校验登录态，无数据直接跳转登录页。
- **我的中心**：`MyFragment` 展示头像、用户名、邮箱、常用入口以及退出登录；个人资料 / 收藏 / 去过 / 订单 / 购物车都提供骨架屏和面包屑副标题。
- **统一 UI 与状态管理**：`BaseFeedFragment`、`FeedAdapter`、空态和骨架屏组件保证相同体验；Toast / 副标题 / 安全区处理所有页面一致。

### 3.3 特色模块（AI / 地图 / 定位）
- **地图服务说明**：内置高德 3D Map，使用 `MapPrivacyHelper` 自动弹出隐私合规对话框，`MapMarkerRenderer` 根据景点标题 + Glide 缩略图生成自定义 Marker。
- **定位服务说明**：`AMapLocationClient` 高频率更新当前位置，合并 bounds 后自动缩放镜头；权限关闭时弹出中文提示。
- **AI 接口**：当前版本未接入 AI，但在 API 层留有扩展参数位，可在后续版本引入智能推荐 / 行程规划。

## 4. 技术架构（Tech Stack）

### 4.1 Android 技术栈
- Java 11 + Android SDK 36，Activity / Fragment 组合管理导航。
- Material Components、AppCompat、ConstraintLayout、RecyclerView、SwipeRefreshLayout 构建 UI 与交互。
- Glide、Shimmer 提供图片加载与骨架屏；`CircularProgressIndicator` 呈现加载状态。
- 自研 Repository 层 + `ApiClient`（HttpURLConnection）保证网络通信、分页与异常处理。
- SharedPreferences 封装为 `UserPreferences`，`UserProfile` 负责反序列化用户模型。

### 4.2 后端技术栈
- 依据 `app/API_DOC.md` 与 `app.py`，后端为 Python Flask 示例服务，统一响应 `{code,msg,data}`，存储可接 SQLite / Mock 数据源。
- 认证暂不发 token，返回完整用户 JSON，由客户端缓存；所有接口使用 JSON 请求体并遵循 snake_case。

### 4.3 第三方服务
- **高德 3D Map & 定位 SDK**：地图渲染、Marker、自定义缩放 / 罗盘。
- **Glide**：网络图片缓存及 Marker 缩略图生成。
- **Material Design 组件库**：按钮、Tab、TextInputLayout、骨架屏样式。

### 4.4 系统架构（文字版）
- **表示层**：`MainActivity` + 五大 Fragment（Home / Mall / Booking / Map / My）+ 活动详情页。
- **数据层**：`AuthRepository`、`TravelRepository`、`UserCenterRepository` 负责调用 API。
- **本地层**：`UserPreferences` 写入 `travelmap_user_pref` SharedPreferences，供启动登录态校验。
- **扩展点**：Map 模块、购物车与订单等二级页面均解耦，可按模块替换或扩展。

## 5. 安装与使用教程（Installation & Usage）

### 5.1 Android APK 安装
1. 克隆仓库并安装 Android Studio Iguana 以上版本。
2. 执行 `./gradlew assembleDebug`，APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。
3. 允许“未知来源”，把 APK 拷贝到手机安装；首次启动需授予定位、网络权限。
4. 如需真机调试，执行 `./gradlew installDebug` 直接推送到已连接设备。

### 5.2 使用流程简介
1. **注册 / 登录**：输入用户名 + 密码即可登录，成功后本地缓存用户 JSON。
2. **浏览与搜索**：在首页 / 商城 / 预订 Tab 搜索关键字，向下滚动加载更多卡片。
3. **地图查看**：切换“行程 / 地图”查看所有景点坐标，点击 Marker 进入详情。
4. **交易流程**：在详情页加入购物车 → 打开购物车核对 → 提交后在订单页查看状态。
5. **个人中心**：在“我的”页查看资料、收藏、去过记录，必要时退出登录。

## 6. 下载地址（Download）

### 6.1 APK 下载
- **最新版本**：`app/build/outputs/apk/debug/app-debug.apk`（CI 可产出同路径 release 包）。
- **历史版本**：按 Git Tag 区分，构建输出保留在 `app/build/outputs/apk/<variant>/`。
- **安全校验**：执行 `shasum -a 256 app-debug.apk` 生成校验值，随发布一同提供。

### 6.2 Git 仓库
- **仓库地址**：TravelMap/Android（当前私有仓库，main 分支为主干）。
- **分支说明**：`main` 保持稳定，功能分支遵循 `feature/<module>` 命名。
- **Issue & PR 规则**：PR 描述需包含变更目的、涉及模块、运行过的 `./gradlew lint`、`./gradlew testDebugUnitTest` 以及 UI 截图；未通过 lint / 单测 / 仪器测试不得合并。

## 7. 隐私政策与权限说明（Privacy & Permissions）
- **数据类型**：登录后缓存完整 `user` JSON（含用户名、昵称、手机、邮箱、头像 URL）。
- **采集隐私数据**：定位仅用于在 Map 页面显示当前坐标，不会上传服务器；其余接口只使用账号信息。
- **权限用途**：
  - 定位（精确 / 粗略）：用于地图和周边展示。
  - 网络：访问后端 API、下载图片与地图瓦片。
  - 存储（间接）：SharedPreferences 持久化登录态。
- **数据保护**：缓存全部保存在 `travelmap_user_pref`，退出登录即调用 `UserPreferences.clear()` 清除；后台不采集额外第三方数据。
- **联系渠道**：`support@travelmap.app`（可替换为实际邮箱）。

## 8. 更新日志（Changelog）
### v1.0.0（2025-11-15）
- 初始版本，包含首页 / 商城 / 预订 Feed。
- 上线高德地图页与实时定位。
- 支持景点收藏、去过、评分以及商品购物车 / 订单流程。
- 实现完整登录 / 注册 / 我的中心与骨架屏体验。

## 9. 常见问题（FAQ）
1. **安装 APK 提示风险？** Android 会提醒来自企业签名的 APK，确认来源可信即可继续安装。
2. **地图无法定位怎么办？** 确认授予精确定位权限，若仍失败可在系统设置中清除 App 权限重新打开；地图页也支持手动拖动查看。
3. **接口超时或加载空白？** 检查是否能访问 `BuildConfig.API_BASE_URL`（当前为 `http://138.68.59.41:5001`），必要时在 `local.properties` 中指向内网服务。
4. **如何清除缓存重新登录？** 进入“我的”页点击“退出登录”，`UserPreferences` 会立即清空用户信息并跳回登录页。
5. **拉取代码后无法编译？** 执行 `./gradlew wrapper --gradle-version 8.7` 确保 Wrapper 完整，Android Studio 中同步 Gradle 并重启。

## 10. 关于作者 / 团队（About Developer）
- **团队**：TravelMap 学生创新小组（产品 / 设计 / Android / 后端 - Justyn）。
- **职责**：Android 负责客户端 UI、地图、高德 SDK；后端维护 `app.py` 接口与 Mock 数据；设计输出 Material 3 视觉稿。
- **联系方式**：`1046220903@qq.com` 或 GitHub Issues。

## 11. 开源协议（License）
- 当前版本仅供课程设计展示与内部 demo 使用，版权归 TravelMap 团队所有；若需二次分发请先取得授权。
- 后续若迁移到开源，请遵守指定的 MIT / Apache-2.0 等协议并在仓库根目录附上 LICENSE。

