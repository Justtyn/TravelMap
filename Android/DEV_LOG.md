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
   - 首页接入 `/api/scenics`，商城筛选 `/api/products` 的 `TICKET/TRAVEL`，预订筛选 `HOTEL`，支持下拉刷新与搜索。

## 📋 待开发计划
1. **Feed 详情页跳转**
   - 为首页/商城/预订的卡片补充点击跳转逻辑，串联对应详情页面，传递必要的 `id/type`。
2. **景点/商品详情页**
   - 新增 Scenic/Product Detail Activity，展示完整字段（图文、地址、经纬度、价格、库存等），支持 Material3 UI。
3. **收藏 / 去过 / 加购交互**
   - 详情页内实现收藏/取消收藏、去过状态切换（含 1-5 评分对话框）、加入购物车并提示成功。
4. **购物车下单体验**
   - 提供购物车列表页、空状态、下单按钮禁用/启用逻辑及成功页/提示。
5. **搜索与筛选增强**
   - 搜索框与后端接口参数对齐（城市、价格区间等），补充“清空筛选/排序”交互。
6. **Banner 动态化**
   - Banner 区域现为占位图，后续需根据运营配置或接口返回动态展示图片、跳转链接。
7. **列表骨架屏与错误态**
   - 新增骨架屏/错误页组件，统一处理网络失败、空数据、无网络等场景，提升体验。

> 以上任务按 Home → Mall → Booking 顺序推进，每个页面完成后同步记录 UI/接口状态。
