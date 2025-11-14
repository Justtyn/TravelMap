# 开发日志（Dev Log）

## ✅ 已实现功能
1. **登录注册**
   - `LoginActivity` 调用 `AuthRepository` 完成账号密码登录，成功后缓存 `user` JSON 并跳转主页。
   - `RegisterActivity` 可创建新账号，所有接口遵循 `app/API_DOC.md`。
2. **用户会话**
   - `UserPreferences`/`UserProfile` 封装 SharedPreferences 读写，`MainActivity` 启动即校验登录态。
   - “我的”页展示头像、用户名、昵称，支持登出、基础菜单占位。
3. **导航框架**
   - `MainActivity` 管理五个底部导航页（首页/商城/预订/行程/我的），Fragment 使用缓存避免重复创建。
4. **首页/商城/预订 UI 与数据**
   - 新增统一的 `BaseFeedFragment`、`FeedAdapter`、`TravelRepository`，实现搜索栏 + Banner + 列表卡片。
   - 首页接入 `/api/scenics`，商城筛选 `/api/products` 的 `TICKET/TRAVEL`，预订筛选 `HOTEL`，支持下拉刷新与搜索，并已接通卡片点击 -> 对应详情页。
5. **用户中心与订单/收藏**
   - “我的”页支持圆角头像、邮箱展示以及快捷菜单，跳转到个人资料编辑、收藏、去过、订单、购物车等页面。
   - `UserInfoActivity`、`FavoritesActivity`、`VisitedActivity`、`OrdersActivity`、`CartActivity` 等已完成基础 UI 与数据加载（收藏/去过/购物车列表、下单、个人信息修改等），并与 `UserCenterRepository` 统一管理接口调用。
6. **详情页与交互完善**
   - 新增 `ScenicDetailActivity`、`ProductDetailActivity` 及其布局，展示图文、地址、经纬度、价格、库存等字段，并通过骨架屏 + Material3 UI 提升加载体验。
   - 收藏/取消收藏、去过记录（含评分弹窗）、加入购物车等操作均接入真实接口，按钮在请求中展示局部 `CircularProgressIndicator`，收藏/去过/加购后的状态即时刷新。
   - 下单成功页 `OrderSuccessActivity`、购物车空态/按钮可用性、收藏/去过/购物车页面均已串联详情页跳转。

## 📋 待开发计划
1. **列表骨架屏与错误态**
   - 当前骨架屏仅在详情页落地，后续需在 Feed/Favorites/Visited 等列表页增加骨架/错误组件，统一处理网络失败、空数据、无网络提示。
2. **购物车与下单体验优化**
   - 进一步美化购物车列表/下单流程（如分组、价格浮层、订单提交动画），并在提交订单时提供更友好的反馈或重试机制。
3. **搜索与筛选增强**
   - 搜索框与后端接口参数对齐（城市、价格区间等），补充“清空筛选/排序”交互。
4. **Banner 动态化**
   - Banner 区域现为占位图，后续需根据运营配置或接口返回动态展示图片、跳转链接。
5. **收藏/去过/订单的状态标签**
   - 在列表卡片上展示“已收藏”“已去过”“订单状态”等标签，便于用户在列表中直接获取状态信息。

> 以上任务按 Home → Mall → Booking 顺序推进，每个页面完成后同步记录 UI/接口状态。
