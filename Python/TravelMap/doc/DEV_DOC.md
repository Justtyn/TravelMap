
• 后端新接口需求文档

  目标：支持前端新增的详情页交互（收藏/去过/购物车及下单反馈），需补齐以下接口。所有接口继续遵循现有响应结构 {"code":200,"msg":"OK","data":...}，返回的数据字段请与现有风格一致。

  ———

  ### 1. 用户资料更新接口

  PUT /api/users/{user_id}

  - 功能：更新用户的手机号、邮箱等可编辑字段（当前仅需 phone、email，后续可扩展昵称等）。
  - 请求体（任意字段可选）：

    {
      "phone": "13800000000",
      "email": "user@example.com"
    }
  - 返回：最新的用户信息 {"user": { ... }}，结构与登录返回一致。
  - 验证：user_id 必须存在；空字符串表示不更新该字段。

  ———

  ### 2. 去过记录删除接口

  DELETE /api/visited/{visited_id}

  - 功能：取消“去过”记录，用于前端“已去过”再次点击取消。
  - 返回：{"visited": { ... 原记录 ... }, "deleted": true}
  - 校验：visited_id 必须属于当前用户（若需要可加 user_id 参数）。

  > 如需按 user_id + scenic_id 删除，也可以提供 DELETE /api/visited?user_id=1&scenic_id=101，以确保幂等。

  ———

  ### 3. 单景点详情接口

  GET /api/scenics/{id}

  - 文档已有列表接口，但前端需要“详情页”展示全部字段（含地址、经纬度、城市、描述、图片等）。
  - 返回：{"data": {... scenic 全字段 ...}}
  - 若已存在，请确认字段完整并在文档中明确。

  ———

  ### 4. 单商品详情接口

  GET /api/products/{id}

  - 同上，需返回完整字段（包含 price、stock、type、hotel_address、description、cover_image 等），供商城/预订详情页展示。

  ———

  ### 5. 收藏状态查询/删除扩展

  目前只有 /api/favorites/products|scenics 列表。为了在详情页快速判断是否已收藏，建议：

  - 可选方案 A：保留列表接口，由前端查询并判断。
  - 可选方案 B：新增 GET /api/favorites/status?user_id=1&target_id=101&target_type=SCENIC，返回 {"favorited": true/false}。
  - 无论哪种方案，需要确保 DELETE /api/favorites 已处理好 idempotent 场景（目前通过 user_id+target 组合删除即可）。

  ———

  ### 6. 购物车下单成功反馈

  当前 POST /api/orders 返回订单详情，但前端希望在下单后展示成功页或跳转。要求：

  - 确认返回数据包含字段：
      - order.id
      - order.order_no
      - order.total_price
      - order.items（含 product 信息）
  - 特别说明：订单成功后清空购物车的逻辑已存在，前端会根据该返回展示“下单成功”消息，无需额外接口。

  ———

  ### 7. （可选）购物车数量更新 & 删除接口

  若后续需要管理购物车条目，请保持 PUT /api/cart/{cart_id}、DELETE /api/cart/{cart_id} 可用，并在文档中列出。

  ———

  注意事项

  1. 所有新接口需更新 /app/API_DOC.md，保持字段、示例与现有风格一致。