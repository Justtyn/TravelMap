# TravelMap 后端接口文档

本文档与 `app.py` 同步维护，所有示例均基于当前实现的真实字段与返回结构。除特别说明外，统一使用 `Content-Type: application/json; charset=utf-8`。

## 0. 通用约定
- Base URL：开发模式默认 `http://127.0.0.1:5000`（或 `app.py` 中配置的端口）。
- 统一响应格式：

```json
{
  "code": 200,
  "msg": "OK",
  "data": { ... }   // 失败时 data 可能为 null
}
```

- 常见业务状态码

| code | HTTP | 说明 |
|------|------|------|
| 200  | 200  | 成功 |
| 400  | 400  | 参数错误或业务校验失败（缺字段、购物车为空等） |
| 401  | 401  | 认证失败（用户名或密码错误） |
| 404  | 404  | 资源不存在（景点 / 商品 / 订单 / 收藏等未找到） |

- 数据字段命名全部为 snake_case，时间格式统一 `YYYY-MM-DD HH:MM:SS`，日期为 `YYYY-MM-DD`。
- 本项目目前不要求 token，登录相关接口会直接回传完整用户资料用于前端缓存。

## 1. 用户与认证

### POST /api/auth/register
- 描述：注册本地账号。
- 请求体：
```json
{ "username": "alice", "password": "pwd123", "phone": "13800000000", "email": "alice@example.com", "nickname": "小艾" }
```
- 响应：
```json
{
  "code": 200,
  "msg": "注册成功",
  "data": { "user": {
    "id": 12, "login_type": "LOCAL", "username": "alice",
    "phone": "13800000000", "email": "alice@example.com",
    "nickname": "小艾", "avatar_url": null,
    "wx_unionid": null, "wx_openid": null,
    "wx_access_token": null, "wx_refresh_token": null,
    "wx_token_expires_at": null
  }}
}
```
- 说明：密码使用 Werkzeug 哈希存储；若用户名重复返回 `400 / "用户名已存在"`。

### POST /api/auth/login
- 描述：用户名密码登录。
- 请求体：`{"username":"alice","password":"pwd123"}`
- 响应：`{"data":{"user":{...与注册返回一致...}}}`
- 说明：密码错误返回 `401 / "用户名或密码错误"`。

### POST /api/auth/wechat
- 描述：模拟微信登录，使用 `code` 构造 `wx_openid`，不存在则创建用户。
- 请求体：`{"code":"abc123"}`
- 响应：`{"data":{"user":{...同上...}}}`
- 说明：`code` 缺失返回 `400 / "code 不能为空"`。

### GET /ping
- 描述：健康检查。
- 响应：`{"data":{"msg":"pong"}}`

## 2. 景点 Scenic

### GET /api/scenics
- 描述：景点列表 + 搜索 + 分页。
- 查询参数：`keyword`(模糊 name/description)、`city`、`page`(默认1)、`page_size`(默认10)。
- 响应：
```json
{
  "data": {
    "list": [
      {
        "id": 1, "name": "鼓浪屿84", "city": "厦门",
        "cover_image": "厦门_0.jpg", "description": "...",
        "address": "厦门市热门路84号", "latitude": 24.45,
        "longitude": 118.07, "audio_url": "https://...",
      }
    ],
    "page": 1, "page_size": 10, "total": 24
  }
}
```

### GET /api/scenics/{id}
- 描述：景点详情。
- 返回：单个景点的全部字段；不存在则 404。

### GET /api/scenics/map
- 描述：地图使用的景点全集。
- 返回：`data` 为 scenic 数组，字段同详情（包含经纬度、音频等）。

## 3. 商品与预订 Product / Booking

### GET /api/products
- 描述：商品列表（商城视图）。
- 查询：`keyword`、`type`(`TRAVEL|HOTEL|TICKET`)、`page`、`page_size`。
- 返回：与景点列表相同的分页结构，`list` 为 product 全字段（`id,name,scenic_id,cover_image,price,stock,description,type,hotel_address`）。

### GET /api/products/{id}
- 描述：商品详情；404 表示不存在。

### GET /api/bookings
- 描述：供酒店/门票页面使用的列表，强制要求 `type`。
- 查询：`type`(`HOTEL`/`TICKET`) 必填，`city`（与 scenic.city 匹配）、`page`、`page_size`。
- 返回：
```json
{
  "data": {
    "list": [
      {
        "product": { "id": 5, "name": "西湖 轻奢酒店", "...": "..." },
        "scenic": { "id": 2, "name": "西湖74", "...": "..." }   // scenic_id 为空时为 null
      }
    ],
    "page": 1,
    "page_size": 10,
    "total": 6
  }
}
```

## 4. 行程计划 Trip Plan

### POST /api/plans
- 描述：保存行程。
- 请求体：`{ "user_id": 1, "title": "江南三日游", "start_date": "2025-05-01", "end_date": "2025-05-03", "source": "MANUAL", "content": "{\"days\":3}" }`
- 返回：`{"data":{"plan_id":123}}`
- 说明：`user_id` 必填，否则 400。

### GET /api/plans?user_id=1
- 描述：用户行程列表，按 `create_time` DESC。
- 返回：TripPlan 对象数组（`id,user_id,title,start_date,end_date,source,content,create_time`）。

### GET /api/plans/{id}
- 描述：行程详情；不存在则 404。

## 5. 收藏 Favorite

### POST /api/favorites
- 描述：收藏景点或商品。
- 请求体：`{"user_id":1,"target_id":101,"target_type":"SCENIC"}`（`target_type` 取 `SCENIC` / `PRODUCT`）。
- 返回：
```json
{
  "data": {
    "favorite": {
      "favorite_id": 9,
      "user_id": 1,
      "target_id": 101,
      "target_type": "SCENIC",
      "create_time": "2025-05-10 12:00:00",
      "target": { ... 完整 scenic 或 product 对象 ... }
    }
  }
}
```
- 说明：若已存在，返回 `200 / "已收藏"` 并附带现有 `favorite`。

### DELETE /api/favorites
- 描述：取消收藏。
- 请求体同上。
- 返回：`{"data":{"favorite":{...被删除记录...},"deleted":true}}`；若记录不存在则 404。

### GET /api/favorites/scenics?user_id=1
### GET /api/favorites/products?user_id=1
- 描述：我的收藏（景点/商品）。
- 返回：`favorite` 对象数组（含 target）。

## 6. 去过记录 Visited

### POST /api/visited
- 描述：标记去过景点，可附带评分。
- 请求体：`{"user_id":1,"scenic_id":101,"rating":5}`
- 返回：
```json
{
  "data": {
    "visited": {
      "visited_id": 3,
      "user_id": 1,
      "scenic_id": 101,
      "visit_date": "2025-05-10",
      "rating": 5,
      "scenic": { ... scenic 全字段 ... }
    }
  }
}
```

### GET /api/visited?user_id=1
- 描述：获取个人“去过”列表。
- 返回：`visited` 对象数组，同上结构；`user_id` 缺失返回 400。

## 7. 购物车 Cart

### POST /api/cart
- 描述：加入购物车；若已存在同一 product 则累加数量。
- 请求体：`{"user_id":1,"product_id":201,"quantity":2}`，`quantity` 可省略（默认 1）但必须 >0。
- 返回：`{"data":{"cart_item":{"cart_id":5,"user_id":1,"quantity":3,"create_time":"...","product":{...}}}}`

### PUT /api/cart/{cart_id}
- 描述：修改数量。
- 请求体：`{"quantity":5}`
- 成功返回更新后的 `cart_item`；若条目不存在则 404。

### DELETE /api/cart/{cart_id}
- 描述：删除条目。
- 返回：`{"data":{"cart_item":{...被删条目...},"deleted":true}}`；不存在则 404。

### GET /api/cart?user_id=1
- 描述：查看购物车。
- 返回：`data` 为 `cart_item` 数组，按 `create_time DESC` 排序。

## 8. 订单 Order

### POST /api/orders
- 描述：从购物车生成订单，成功后清空该用户购物车。
- 请求体（视业务类型可传联系人信息）：
```json
{
  "user_id": 1,
  "contact_name": "张三",
  "contact_phone": "13800138000",
  "order_type": "HOTEL",
  "checkin_date": "2025-11-20",
  "checkout_date": "2025-11-21"
}
```
- 返回：
```json
{
  "data": {
    "order": {
      "id": 18,
      "order_no": "20240510120000abcdef",
      "user_id": 1,
      "order_type": "HOTEL",
      "total_price": 2997.0,
      "status": "CREATED",
      "create_time": "2025-05-10 12:00:00",
      "pay_time": null,
      "contact_name": "张三",
      "contact_phone": "13800138000",
      "checkin_date": "2025-11-20",
      "checkout_date": "2025-11-21",
      "items": [
        {
          "order_item_id": 31,
          "order_id": 18,
          "product_id": 2,
          "quantity": 3,
          "price": 999.0,
          "product": { ... product 全字段 ... }
        }
      ]
    }
  }
}
```
- 说明：购物车为空返回 `400 / "购物车为空"`。

### GET /api/orders?user_id=1
- 描述：用户订单列表。
- 返回：订单数组，每个订单包含 `items` 子数组（结构同上）。

### GET /api/orders/{id}
- 描述：订单详情；不存在返回 404。
- 返回：`{"data":{"order":{...含 items ...}}}`

## 9. 数据库表概览

| 表 | 关键字段 | 说明 |
|----|----------|------|
| user | id, login_type, username, password, phone, email, nickname, avatar_url, wx_* | 存储本地与微信用户信息（密码仅存哈希，接口不回传）。 |
| scenic | id, name, city, cover_image, description, address, latitude, longitude, audio_url | 景点基础资料。 |
| product | id, name, scenic_id, cover_image, price, stock, description, type, hotel_address | 商品/酒店/门票统一表。 |
| favorite | id, user_id, target_id, target_type, create_time | 收藏记录；应用层返回时附带 target 明细。 |
| visited | id, user_id, scenic_id, visit_date, rating | 去过记录，JOIN scenic 后返回。 |
| trip_plan | id, user_id, title, start_date, end_date, source, content, create_time | 行程计划。 |
| cart_item | id, user_id, product_id, quantity, create_time | 购物车条目，接口返回带 product 详情。 |
| order_main | id, order_no, user_id, order_type, total_price, status, create_time, pay_time, contact_name, contact_phone, checkin_date, checkout_date | 订单主表。 |
| order_item | id, order_id, product_id, quantity, price | 订单明细，接口中与 product 关联返回。 |

## 10. 冒烟与压力测试 (tests_smoke.py)

- 运行：`python tests_smoke.py`
- 功能：自动向 SQLite 注入随机景点/商品样本（若为空），使用 `app.test_client()` 依次调用所有接口，覆盖成功场景、常见错误与分页/压力循环。
- 覆盖点：注册/登录/微信登录、scenic 列表+分页、product/booking 过滤、行程 CRUD、收藏 CRUD、去过 CRUD、购物车 CRUD、订单创建/列表/详情、错误分支（缺参数、资源不存在、购物车为空、quantity<=0 等）。
- 输出：每个步骤会打印 `[LABEL] status=... code=... msg=...`，末尾出现 `== 冒烟 + 压力测试完成 ==` 视为通过。

## 11. 文档维护建议
1. 任何接口结构更新，需同步修改 `app.py` 对应注释与本文件的请求/响应示例。
2. 若返回体增加字段，请更新相应段落的示例 JSON，确保前端可直接参考。
3. 对外暴露接口的新增/下线，务必在本文件中显式标记（例如添加“已废弃”说明）。
4. 建议在 PR 模板中加入“API_DOC 是否更新”检查项，确保此文档始终与实现一致。

---
最后更新：2025-05-10。当前版本已覆盖 `app.py` 中全部接口。
