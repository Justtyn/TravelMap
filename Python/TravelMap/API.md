好，那我就**完全按你现在这份 SQLite 数据库**来设计一整套后端接口规范，方便你用 Flask 去实现，也方便你写课程设计说明书。
（我已经读过你最新的 SQL 文件：`user / scenic / product / favorite / visited / cart_item / order_main / order_item / trip_plan` 这些表结构。）

下面所有接口都统一返回格式示例：

```json
{
  "code": 200,
  "msg": "OK",
  "data": { ... }   // 或列表
}
```

你也可以定义为 `success: true/false`，自己固定一种风格就行。

---

## 一、用户登录注册模块（表：user）

### 1. 注册

**POST `/api/auth/register`**

* 功能：创建本地账号（先实现这个，微信登录可以后面加）。
* 请求体（JSON）：

```json
{
  "username": "raphael",
  "password": "123456",
  "phone": "13800000000",
  "email": "xxx@xxx.com",
  "nickname": "小王"
}
```

* 后端逻辑：

  * 检查 `username` 是否已存在。
  * `password` 使用哈希（例如 `werkzeug.security.generate_password_hash`）。
  * 插入 `user` 表：`login_type='LOCAL'`，其他字段任选填。

* 响应示例：

```json
{
  "code": 200,
  "msg": "注册成功",
  "data": {
    "user_id": 1
  }
}
```

---

### 2. 登录（本地账号）

**POST `/api/auth/login`**

* 请求体：

```json
{
  "username": "raphael",
  "password": "123456"
}
```

* 后端逻辑：

  * 从 `user` 表按 `username` 查出一条记录。
  * 校验密码哈希。
  * 登录成功后：

    * 简单做法：直接返回 `user_id`；
    * 稍微正规一点：生成一个 `token`（JWT 或随机字符串），存在 Redis/数据库，这个看你时间。

* 响应示例：

```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "user_id": 1,
    "nickname": "小王",
    "avatar_url": "https://xxx",
    "token": "xxx"   // 如果你实现了
  }
}
```

---

### 3. 微信登录（预留接口）

**POST `/api/auth/wechat`**

* 请求体：

```json
{
  "code": "微信客户端回调给你的code"
}
```

* 后端逻辑（真实流程写在说明书里即可）：

  1. 用 `code + appid + secret` 请求微信 `/sns/oauth2/access_token`。
  2. 拿到 `openid/unionid`，查 `user.wx_openid` 是否存在：

     * 不存在：创建新用户，`login_type='WECHAT'`，保存 `wx_openid`、`wx_unionid` 等。
  3. 返回你自己系统的 `user_id` 和 `token`。

* 响应示例：

```json
{
  "code": 200,
  "msg": "微信登录成功",
  "data": {
    "user_id": 2,
    "nickname": "微信昵称",
    "avatar_url": "https://wx.qlogo.cn/xxx",
    "token": "xxx"
  }
}
```

> 课程如果没强制要真接入微信 SDK，可以用这个接口做“伪实现”，重点展示你理解 OAuth2 流程。

---

## 二、景点列表首页（表：scenic）

### 1. 景点列表 + 搜索

**GET `/api/scenics`**

* 查询参数：

  * `keyword`（可选，模糊搜索 `name` 和 `description`）
  * `city`（可选，用于城市筛选）
  * `page`、`page_size`（可选，分页）

示例：

`GET /api/scenics?keyword=鼓浪屿&city=厦门&page=1&page_size=10`

* 响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "鼓浪屿",
        "city": "厦门",
        "cover_image": "https://xxx",
        "description": "XXXX",
        "address": "厦门市思明区XXX",
        "latitude": 24.448,
        "longitude": 118.123,
        "audio_url": "https://xxx/audio1.mp3"
      }
    ],
    "page": 1,
    "page_size": 10,
    "total": 1
  }
}
```

---

### 2. 景点详情

**GET `/api/scenics/{id}`**

* 示例：`GET /api/scenics/1`
* 返回：

```json
{
  "code": 200,
  "msg": "OK",
  "data": {
    "id": 1,
    "name": "鼓浪屿",
    "city": "厦门",
    "cover_image": "https://xxx",
    "description": "详细介绍...",
    "address": "厦门市思明区XXX",
    "latitude": 24.448,
    "longitude": 118.123,
    "audio_url": "https://xxx/audio1.mp3"
  }
}
```

---

### 3. 地图用景点简要列表

**GET `/api/scenics/map`**

* 功能：地图页需要所有景点的坐标 & 缩略图。
* 响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": [
    {
      "id": 1,
      "name": "鼓浪屿",
      "latitude": 24.448,
      "longitude": 118.123,
      "cover_image": "https://xxx"
    },
    ...
  ]
}
```

---

## 三、商城列表页（旅行商品，表：product）

> 你现在 `product.type` 是 TEXT，可以用：
>
> * `TRAVEL`：旅行周边商品
> * `TICKET`：门票
> * `HOTEL`：酒店

### 1. 商城商品列表（旅行商品）

**GET `/api/products`**

* 查询参数：

  * `keyword`（可选）
  * `type`（可选，不传 = 全部；商城页传 `TRAVEL`）
  * `page`、`page_size`

商城列表页请求示例：

`GET /api/products?type=TRAVEL&page=1&page_size=10`

* 响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": {
    "list": [
      {
        "id": 10,
        "name": "鼓浪屿纪念T恤",
        "scenic_id": 1,
        "cover_image": "https://xxx",
        "price": 99.0,
        "stock": 100,
        "description": "纯棉T恤...",
        "type": "TRAVEL",
        "hotel_address": null
      }
    ],
    "page": 1,
    "page_size": 10,
    "total": 1
  }
}
```

### 2. 商品详情

**GET `/api/products/{id}`**

* 示例：`GET /api/products/10`
* 响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": {
    "id": 10,
    "name": "鼓浪屿纪念T恤",
    "scenic_id": 1,
    "cover_image": "https://xxx",
    "price": 99.0,
    "stock": 100,
    "description": "详细说明...",
    "type": "TRAVEL",
    "hotel_address": null
  }
}
```

---

## 四、预订列表页（酒店 / 门票）

其实直接基于 `product` 表，用 `type='HOTEL'` 或 `'TICKET'` 即可。

### 1. 预订列表：酒店 / 门票

**GET `/api/bookings`**

* 查询参数：

  * `type`：`HOTEL` / `TICKET`（必选之一）
  * `city`（可选：可以通过关联 `scenic.city` 做筛选，可视时间实现）
  * `page`、`page_size`

示例：`GET /api/bookings?type=HOTEL&page=1&page_size=10`

* 后端 SQL 简单写法：

  * 方案 1：只看自己的字段：`SELECT * FROM product WHERE type='HOTEL'`
  * 方案 2：关联景点城市：`JOIN scenic ON product.scenic_id = scenic.id AND scenic.city=?`

* 响应结构与 `/api/products` 基本一样，只是语义上“预定列表”。

---

## 五、计划列表页（行程计划，表：trip_plan）

### 1. 新增 / 保存行程计划

**POST `/api/plans`**

* 请求体（`content` 推荐用 JSON 字符串，里面放每天的 scenic_id 列表）：

```json
{
  "user_id": 1,
  "title": "厦门2日游·AI推荐",
  "start_date": "2025-11-20",
  "end_date": "2025-11-21",
  "source": "AI",
  "content": "{ \"days\": [ {\"day\":1, \"scenics\":[1,2]}, {\"day\":2, \"scenics\":[3,4]} ] }"
}
```

* 插入 `trip_plan` 表。
* 响应：

```json
{
  "code": 200,
  "msg": "保存成功",
  "data": {
    "plan_id": 3
  }
}
```

---

### 2. 查询当前用户的行程计划列表

**GET `/api/plans`**

* 查询参数：

  * `user_id`（必传）

示例：`GET /api/plans?user_id=1`

* 响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": [
    {
      "id": 3,
      "user_id": 1,
      "title": "厦门2日游·AI推荐",
      "start_date": "2025-11-20",
      "end_date": "2025-11-21",
      "source": "AI",
      "content": "{...}",
      "create_time": "2025-11-14 10:00:00"
    }
  ]
}
```

### 3. 行程详情（可选）

**GET `/api/plans/{id}`**

---

## 六、个人中心相关接口

### 6.1 收藏页（表：favorite + scenic/product）

#### 1）添加收藏

**POST `/api/favorites`**

* 请求体：

```json
{
  "user_id": 1,
  "target_id": 1,
  "target_type": "SCENIC"   // 或 "PRODUCT"
}
```

* 写入 `favorite` 表（如果已存在，可以直接返回“已收藏”）。

#### 2）取消收藏

**DELETE `/api/favorites`**

* 请求体：

```json
{
  "user_id": 1,
  "target_id": 1,
  "target_type": "SCENIC"
}
```

#### 3）我的收藏景点列表

**GET `/api/favorites/scenics`**

* 参数：`user_id`
* 后端：`favorite` JOIN `scenic`

```json
{
  "code": 200,
  "msg": "OK",
  "data": [
    {
      "id": 1,
      "name": "鼓浪屿",
      "city": "厦门",
      "cover_image": "https://xxx",
      "address": "xxx",
      "latitude": 24.4,
      "longitude": 118.1
    }
  ]
}
```

#### 4）我的收藏商品列表

**GET `/api/favorites/products`**

* 参数：`user_id`
* 后端：`favorite` JOIN `product`

---

### 6.2 去过页面（表：visited + scenic）

#### 1）标记去过

**POST `/api/visited`**

* 请求体：

```json
{
  "user_id": 1,
  "scenic_id": 1,
  "rating": 5   // 可选
}
```

* 写入 `visited` 表，`visit_date` 可用 `date("now")`。

#### 2）查询我的“去过”

**GET `/api/visited`**

* 参数：`user_id`
* 后端：`visited` JOIN `scenic`
* 响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": [
    {
      "scenic_id": 1,
      "name": "鼓浪屿",
      "city": "厦门",
      "cover_image": "https://xxx",
      "visit_date": "2025-11-10",
      "rating": 5
    }
  ]
}
```

---

### 6.3 订单页（表：order_main + order_item + product）

#### 1）创建订单（从购物车生成，也可以支持直接购买）

**POST `/api/orders`**

* 请求体（从购物车结算）：

```json
{
  "user_id": 1,
  "contact_name": "张三",
  "contact_phone": "13800000000",
  "order_type": "PRODUCT",        // 或 "HOTEL"
  "checkin_date": "2025-11-20",   // 酒店用，可空
  "checkout_date": "2025-11-21"   // 酒店用，可空
}
```

* 后端逻辑：

  1. 从 `cart_item` 查出该用户所有商品（包括 HOTEL/TICKET/TRAVEL）。
  2. 计算总价，写入 `order_main`。
  3. 写入 `order_item` 多条。
  4. 删除该用户的 `cart_item`。

* 响应：

```json
{
  "code": 200,
  "msg": "下单成功",
  "data": {
    "order_id": 5,
    "order_no": "202511140001"
  }
}
```

#### 2）我的订单列表

**GET `/api/orders`**

* 参数：`user_id`
* 响应（只展示主信息）：

```json
{
  "code": 200,
  "msg": "OK",
  "data": [
    {
      "id": 5,
      "order_no": "202511140001",
      "order_type": "HOTEL",
      "total_price": 699.0,
      "status": "CREATED",
      "create_time": "2025-11-14 10:30:00",
      "checkin_date": "2025-11-20",
      "checkout_date": "2025-11-21"
    }
  ]
}
```

#### 3）订单详情

**GET `/api/orders/{id}`**

* 返回 `order_main` + 对应 `order_item` + 商品信息：

```json
{
  "code": 200,
  "msg": "OK",
  "data": {
    "order": {
      "id": 5,
      "order_no": "202511140001",
      "order_type": "HOTEL",
      "total_price": 699.0,
      "status": "PAID",
      "create_time": "2025-11-14 10:30:00",
      "checkin_date": "2025-11-20",
      "checkout_date": "2025-11-21",
      "contact_name": "张三",
      "contact_phone": "13800000000"
    },
    "items": [
      {
        "product_id": 20,
        "name": "XX酒店大床房一晚",
        "cover_image": "https://xxx",
        "quantity": 1,
        "price": 699.0,
        "type": "HOTEL"
      }
    ]
  }
}
```

---

### 6.4 购物车页（包含商城 + 预定的信息，表：cart_item + product）

#### 1）添加到购物车

**POST `/api/cart`**

* 请求体：

```json
{
  "user_id": 1,
  "product_id": 10,
  "quantity": 2
}
```

> 不区分商城 / 预定，因为它们都在 `product` 表里，通过 `type` 区分，购物车统一处理。

#### 2）修改数量

**PUT `/api/cart/{cart_id}`**

* 请求体：

```json
{
  "quantity": 3
}
```

#### 3）删除购物车项

**DELETE `/api/cart/{cart_id}`**

#### 4）查询当前用户购物车

**GET `/api/cart`**

* 参数：`user_id`
* 响应：

```json
{
  "code": 200,
  "msg": "OK",
  "data": [
    {
      "cart_id": 1,
      "quantity": 2,
      "product": {
        "id": 10,
        "name": "鼓浪屿纪念T恤",
        "cover_image": "https://xxx",
        "price": 99.0,
        "type": "TRAVEL"
      }
    },
    {
      "cart_id": 2,
      "quantity": 1,
      "product": {
        "id": 20,
        "name": "XX酒店大床房一晚",
        "cover_image": "https://xxx",
        "price": 699.0,
        "type": "HOTEL",
        "hotel_address": "厦门市XXX路XX号"
      }
    }
  ]
}
```

在安卓端就可以：

* 把 `type='HOTEL' / 'TICKET'` 的商品显示在“预定”一段；
* 其它的显示在“商城商品”一段，实现“一个购物车，两个分类”。

---