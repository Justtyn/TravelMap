# TravelMap 后端接口文档

本文面向前端与联调同学，所有示例均来自当前 `app.py` 实现。未说明时均使用 `Content-Type: application/json; charset=utf-8`。

## 0. 通用约定
- **Base URL**：开发模式默认 `http://127.0.0.1:5001`（若 `app.py` 调整端口，请以实际端口为准）。
- **统一响应格式**
  ```json
  {
    "code": 200,
    "msg": "OK",
    "data": { ... }   // 失败时 data 可能为 null
  }
  ```
- **常用业务状态码**

  | code | HTTP | 说明 |
  |------|------|------|
  | 200  | 200  | 成功 |
  | 400  | 400  | 参数或业务校验失败（缺字段、购物车为空等） |
  | 401  | 401  | 认证失败（用户名或密码错误） |
  | 404  | 404  | 资源不存在（景点 / 商品 / 订单等未找到） |

- **字段规范**：全部 `snake_case`；日期 `YYYY-MM-DD`，时间戳 `YYYY-MM-DD HH:MM:SS`。
- **认证**：当前版本不发放 token，登录/微信登录直接返回完整用户资料供前端缓存。
- **商品类型**：`TICKET`（门票）、`HOTEL`（酒店/预约类）、`TRAVEL`（旅行体验、文创周边等）。

---

## 1. 用户与认证

### POST /api/auth/register
- **说明**：注册本地账号，用户名需唯一。
- **请求体**
  ```json
  {
    "username": "alice",
    "password": "pwd123",
    "phone": "13800000000",
    "email": "alice@example.com",
    "nickname": "小艾"
  }
  ```
- **成功响应**
  ```json
  {
    "code": 200,
    "msg": "注册成功",
    "data": { "user": { "id": 12, "login_type": "LOCAL", "username": "alice", ... } }
  }
  ```
- **备注**：用户名已存在 → `400 / "用户名已存在"`。

### POST /api/auth/login
- **说明**：用户名 + 密码登录。
- **请求体**：`{"username":"alice","password":"pwd123"}`
- **成功响应**：`{"code":200,"msg":"登录成功","data":{"user":{...完整用户信息...}}}`
- **备注**：密码错误 → `401 / "用户名或密码错误"`。

### POST /api/auth/wechat
- **说明**：模拟微信登录，使用 `code` 构造 mock openid，首次会创建 `login_type=WECHAT` 用户。
- **请求体**：`{"code":"abc123"}`（必填）
- **成功响应**：与登录一致。
- **备注**：缺 `code` → `400 / "code 不能为空"`。

### GET /ping
- **说明**：健康检查，用于 Heartbeat/部署探针。
- **响应**：`{"code":200,"msg":"OK","data":{"msg":"pong"}}`

---

## 2. 景点 Scenic

### GET /api/scenics
- **说明**：景点列表 + 搜索，无分页，直接返回全部匹配记录（按 id 升序）。
- **查询参数**
  | 名称 | 必填 | 说明 |
  |------|------|------|
  | `keyword` | 否 | 模糊匹配 `name` 与 `description` |
  | `city` | 否 | 精确匹配城市名 |
- **响应示例**
  ```json
  {
    "code": 200,
    "msg": "OK",
    "data": [
      {
        "id": 17,
        "name": "故宫博物院",
        "city": "北京",
        "cover_image": null,
        "description": "世界文化遗产...",
        "address": "北京市东城区景山前街4号",
        "latitude": 39.9163,
        "longitude": 116.3972,
        "audio_url": null
      }
    ]
  }
  ```

### GET /api/scenics/{id}
- 返回单个景点的完整字段；无记录 → `404 / "景点不存在"`。

### GET /api/scenics/map
- 返回全部景点数组（与列表字段一致），常用于地图落点。

---

## 3. 商品与预订 Product / Booking

### GET /api/products
- **说明**：商品/门票/酒店/体验统一列表，未分页，按 id 升序。
- **查询参数**：`keyword`（模糊名称/描述）、`type`（可选，取值 `TRAVEL | HOTEL | TICKET`）。
- **响应**：`data` 为 product 数组，字段 `id,name,scenic_id,cover_image,price,stock,description,type,hotel_address`。

### GET /api/products/{id}
- 单个商品详情；无记录 → `404 / "商品不存在"`。

### GET /api/bookings
- **说明**：前端“预订”页面使用（酒店/门票），不分页。
- **查询参数**
  | 名称 | 必填 | 说明 |
  |------|------|------|
  | `type` | 是 | `HOTEL` 或 `TICKET` |
  | `city` | 否 | 关联 `scenic.city`，用于筛选 |
- **响应示例**
  ```json
  {
    "code": 200,
    "msg": "OK",
    "data": [
      {
        "product": { "id": 35, "name": "上海豫园安和里精品酒店", "price": 880.0, "type": "HOTEL", ... },
        "scenic":  { "id": 24, "name": "豫园", "city": "上海", ... }
      },
      {
        "product": { "id": 33, "name": "黄浦江夜游船票·黄金甲", "type": "TICKET", ... },
        "scenic":  { "id": 22, "name": "外滩", ... }
      }
    ]
  }
  ```
- **备注**：若 `product.scenic_id` 为空，则 `scenic` 字段为 `null`。

---

## 4. 行程计划 Trip Plan

### POST /api/plans
- **说明**：保存/新增行程。
- **请求体**
  ```json
  {
    "user_id": 1,
    "title": "江南三日游",
    "start_date": "2025-05-01",
    "end_date": "2025-05-03",
    "source": "MANUAL",
    "content": "{\"days\":3}"
  }
  ```
- **成功响应**：`{"code":200,"msg":"保存成功","data":{"plan_id":123}}`
- **备注**：`user_id` 必填。

### GET /api/plans?user_id=1
- 返回该用户全部行程，按 `create_time DESC` 排序，字段：`id,user_id,title,start_date,end_date,source,content,create_time`。

### GET /api/plans/{id}
- 单个行程详情；无记录 → `404 / "行程不存在"`。

---

## 5. 收藏 Favorite

### POST /api/favorites
- **说明**：收藏景点或商品。
- **请求体**：`{"user_id":1,"target_id":101,"target_type":"SCENIC"}`（`target_type` 可选 `SCENIC` / `PRODUCT`）
- **成功响应**
  ```json
  {
    "code": 200,
    "msg": "收藏成功",
    "data": {
      "favorite": {
        "favorite_id": 9,
        "user_id": 1,
        "target_id": 101,
        "target_type": "SCENIC",
        "create_time": "2025-05-10 12:00:00",
        "target": { ... 完整 scenic 或 product ... }
      }
    }
  }
  ```
- **备注**：若已收藏，则 `msg="已收藏"` 并返回现有记录。

### DELETE /api/favorites
- **说明**：取消收藏。
- **请求体**：同 POST。
- **成功响应**：`{"data":{"favorite":{...被删记录...},"deleted":true}}`；未找到 → `404 / "收藏记录不存在"`。

### GET /api/favorites/scenics?user_id=1
### GET /api/favorites/products?user_id=1
- 返回收藏数组，每项包含 `favorite_*` 字段与嵌套 `target`。

---

## 6. 去过记录 Visited

### POST /api/visited
- **说明**：记录“去过”景点，可附评分。
- **请求体**：`{"user_id":1,"scenic_id":101,"rating":5}`
- **成功响应**
  ```json
  {
    "code": 200,
    "msg": "已标记为去过",
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
- 返回 `visited` 数组（含嵌套 scenic）；缺 `user_id` → `400 / "user_id 必填"`。

---

## 7. 购物车 Cart

### POST /api/cart
- **说明**：加入购物车；若该用户已存在同一个 product，会累加数量。
- **请求体**：`{"user_id":1,"product_id":201,"quantity":2}`（`quantity` 省略则默认 1，但必须 >0）
- **成功响应**：`{"data":{"cart_item":{"cart_id":5,"user_id":1,"quantity":3,"create_time":"...","product":{...}}}}`

### PUT /api/cart/{cart_id}
- **说明**：修改数量。
- **请求体**：`{"quantity":5}`
- **成功响应**：返回最新 `cart_item`；条目不存在 → `404 / "购物车条目不存在"`。

### DELETE /api/cart/{cart_id}
- **说明**：删除条目。
- **响应**：`{"data":{"cart_item":{...被删记录...},"deleted":true}}`；未找到 → 404。

### GET /api/cart?user_id=1
- **说明**：查看购物车。
- **响应**：`data` 为数组，按 `create_time DESC` 排序，每项包含 `product` 子对象。

---

## 8. 订单 Order

### POST /api/orders
- **说明**：从购物车生成订单，成功后清空该用户购物车。
- **请求体**
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
- **成功响应**
  ```json
  {
    "code": 200,
    "msg": "下单成功",
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
- **备注**：购物车为空 → `400 / "购物车为空"`。

### GET /api/orders?user_id=1
- 返回订单数组（按 `create_time DESC`），每个元素自带 `items` 子数组。

### GET /api/orders/{id}
- 单个订单详情；不存在 → `404 / "订单不存在"`。

---

## 9. 数据库表概览

| 表 | 关键字段 | 说明 |
|----|----------|------|
| `user` | id, login_type, username, password, nickname, avatar_url, wx_* | 用户信息（密码仅存哈希，接口不会回传）。 |
| `scenic` | id, name, city, cover_image, description, address, latitude, longitude, audio_url | 景点基础资料。 |
| `product` | id, name, scenic_id, cover_image, price, stock, description, type, hotel_address | 商品/门票/酒店统一表。 |
| `favorite` | id, user_id, target_id, target_type, create_time | 收藏记录，接口会回传 `target` 明细。 |
| `visited` | id, user_id, scenic_id, visit_date, rating | “去过”记录，JOIN scenic 后返回。 |
| `trip_plan` | id, user_id, title, start_date, end_date, source, content, create_time | 行程计划存档，`content` 多为 JSON 字符串。 |
| `cart_item` | id, user_id, product_id, quantity, create_time | 购物车条目，接口会附带 product 信息。 |
| `order_main` | id, order_no, user_id, order_type, total_price, status, create_time, pay_time, contact_name, contact_phone, checkin_date, checkout_date | 订单主表。 |
| `order_item` | id, order_id, product_id, quantity, price | 订单明细（下单时快照价格）。 |

---

## 10. 文档维护注意事项
1. 任何接口结构/字段调整都必须同步更新本文件与 `app.py` 中的注释。
2. 若新增字段，请立即补充请求/响应示例，确保前端无需反复抓包。
3. 如果接口废弃或新增，请在章节中显式标注（例如“已废弃”或“新增于 2025-05-10”）。
4. 建议在 PR 模板中添加“API 文档已更新”勾选项，避免出现实现与文档不一致。

---
最后更新：2025-05-10。当前版本涵盖 `app.py` 中全部接口。
