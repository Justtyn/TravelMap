# 开发说明（Dev Log）

## 一、整体架构与基础设施
1. **登录/注册体系**
   - `LoginActivity` 调用 `AuthRepository` 完成账号登录，成功后通过 `UserPreferences` 缓存用户 JSON，并跳转 `MainActivity`。
   - `RegisterActivity` 对接注册接口，遵循 `app/API_DOC.md` 中的参数规范。注册成功后可直接使用刚创建的账号登录。
2. **用户会话管理**
   - `UserPreferences`/`UserProfile` 封装 SharedPreferences 读写，提供 `saveUser()`、`getUserProfile()`、`clear()` 等方法。
   - `MainActivity` 启动时校验登录态，若无有效用户信息则跳转 `LoginActivity`。
3. **导航与页面容器**
   - `MainActivity` 管理首页（Home）、商城（Mall）、预订（Booking）、行程（Plan，后续将替换）、我的（My）五个 Tab。
   - `BaseFeedFragment` 提供统一的搜索框、Banner、列表、下拉刷新、骨架屏逻辑，`HomeFragment`/`MallFragment`/`BookingFragment` 继承并实现数据加载。
4. **网络层**
   - `ApiClient` 封装 GET/POST/PUT/DELETE 请求，内置 Base URL、超时时间及 JSON 解析。
   - `TravelRepository` 负责景点、商品列表与详情；`UserCenterRepository` 负责收藏、去过、购物车、订单、个人资料等接口。

## 二、首页/商城/预订功能
1. **Feed 列表与搜索**
   - 使用 `FeedAdapter` 渲染 `item_feed_card`，展示封面、标题、描述、地址、经纬度、库存、访问时间、评分等字段。
   - 搜索框支持键盘 Search 与右侧图标触发，列表支持下拉刷新，骨架屏在初次加载时展示三张占位卡片。
2. **详情页跳转**
   - 点击卡片后自动根据 Fragment 类型跳转到 `ScenicDetailActivity`（首页）或 `ProductDetailActivity`（商城/预订），并传递 `EXTRA_SCENIC_ID` / `EXTRA_PRODUCT_ID`。
3. **Banner、空态、错误状态**
   - 各 Fragment 配置不同的 Banner 标题/副标题/图片；当接口返回空列表或异常时，展示定制化空态文字与 Toast。

## 三、景点/商品详情
1. **数据展示**
   - `ScenicDetailActivity`：展示名称、城市、地址、经纬度、描述，骨架屏加载，支持收藏与“去过”切换（含评分弹窗）；顶部 Toolbar 副标题显示“首页 / 景点详情”。
   - `ProductDetailActivity`：展示名称、类型（TICKET/TRAVEL/HOTEL 等）、价格、库存、地址、描述，骨架屏加载，支持收藏与加入购物车；若类型为 HOTEL，则 Toolbar 副标题显示“预订 / 商品详情”，否则显示“商城 / 商品详情”。后续需在经纬度下方加地图组件。
2. **交互**
   - 收藏/去过/加入购物车操作均调用 `UserCenterRepository` 对应接口，并在按钮右侧显示小型 `CircularProgressIndicator`。
   - “去过”支持 1~5 分评分及取消记录；收藏/去过状态变化后实时刷新按钮文案。
3. **地图与位置（待扩展）**
   - 目前仅展示经纬度文本，计划引入地图组件并在详情页中展示。

## 四、我的页面与个人中心
1. **MyFragment**
   - 显示头像、用户名、邮箱、快捷菜单（个人资料、收藏、去过、订单、购物车），骨架屏在首次进入时展示整体占位；“退出登录”按钮使用品牌蓝色背景，点击后清理登录态并跳回登录页。
2. **二级页面（均避让安全区、含骨架屏与面包屑副标题）**
   - `UserInfoActivity`：查看/编辑手机号、邮箱，Toolbar 副标题“我的 / 个人资料”。
   - `FavoritesActivity`：通过 `MaterialButtonToggleGroup` 切换“商品收藏/景点收藏”，顶部显示“我的 / 收藏”；列表骨架 + SwipeRefresh。
   - `VisitedActivity`：展示 `/api/visited` 返回的历史景点，点击可进入景点详情；Toolbar 副标题“我的 / 去过”。
   - `OrdersActivity`：展示 `/api/orders` 列表，下一步需支持订单详情与更多字段；Toolbar 副标题“我的 / 订单”。
   - `CartActivity`：展示购物车条目、下单按钮（调用 `/api/orders`），支持骨架屏；Toolbar 副标题“我的 / 购物车”。后续需支持删除/修改数量、丰富提交字段。
3. **收藏/去过页面点击行为**
   - 收藏页面根据当前 Tab 跳转到 Scenic/Product 详情；去过页面点击进入景点详情。

## 五、购物车与订单
1. **购物车**
   - `CartActivity` 使用 `CartAdapter` 渲染 `item_cart_entry`，展示商品封面、标题、描述、价格标签与数量。
   - 下单调用 `UserCenterRepository.createOrder()`，成功后跳转 `OrderSuccessActivity`（展示订单号、金额）。
   - 当前支持骨架屏加载与空态提示，后续需补充商品数量修改、删除、所有下单字段校验。
2. **订单管理**
   - `OrdersActivity` 列出所有订单（使用 FeedAdapter）；当前仅显示卡片信息，后续需补充订单详情页面。

## 六、UI/UX 统一
1. **骨架屏**
   - 详情页、首页/商城/预订列表、“我的”页、收藏/去过/订单/购物车等全部支持 Shimmer 骨架。
2. **安全区处理**
   - 所有详情页与“我的”二级页面使用 `android:fitsSystemWindows="true"`，Toolbar 自动避让状态栏。
3. **面包屑导航**
   - 各详情页与二级页面的 Toolbar 副标题统一展示当前层级路径，提升定位感。

## 后续重点（新要求）
1. **行程页 → 地图页**
   - 将现有 Plan 页替换为地图视图，展示数据库中所有景点的坐标及当前用户实时定位；废弃原行程接口与 UI。
2. **景点详情地图组件**
   - 在 `ScenicDetailActivity` 中的经纬度下方添加地图组件，直观呈现景点位置。
3. **购物车流程增强**
   - 下单时使用数据库中所有必要字段、支持删除商品与修改数量，流程提示更清晰。
4. **订单详情**
   - `OrdersActivity` 中的每条订单可点击查看详情：状态、收货人、联系方式、子项列表等。

> 以上开发说明涵盖当前所有已实现模块及 UI/交互细节，后续迭代将按“行程页改版 → 景点详情地图 → 购物车增强 → 订单详情”顺序推进。***
