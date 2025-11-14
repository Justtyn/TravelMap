# TravelMap 后端接口文档 (课程设计说明书附录)

统一返回结构：
```json
{ "code": 200, "msg": "OK", "data": ... }
```
错误示例：
```json
{ "code": 400, "msg": "用户名已存在", "data": null }
```

## 1. 用户与认证
| 模块 | 接口 | 方法 | 描述 | 请求参数/体 | 成功返回 data | 备注 |
|------|------|------|------|-------------|---------------|------|
| 用户 | /api/auth/register | POST | 注册本地账号 | JSON: username,password,phone,email,nickname | {"user_id":1} | 密码哈希存储 |
| 用户 | /api/auth/login | POST | 登录 | JSON: username,password | {"user_id":1,"nickname":"","avatar_url":null,"token":"..."} | token 简易 UUID |
| 用户 | /api/auth/wechat | POST | 微信登录占位 | JSON: code | {"user_id":2,"nickname":"微信用户_xxxx","avatar_url":null,"token":"..."} | 模拟 openid |
| 健康 | /ping | GET | 健康检查 | 无 | {"msg":"pong"} | 部署探针 |

## 2. 景点 Scenic
| 接口 | 方法 | 描述 | 查询参数 | data 结构 | 说明 |
|-------|------|------|----------|-----------|------|
| /api/scenics | GET | 景点列表+搜索+分页 | keyword,city,page,page_size | {list:[],page,page_size,total} | 模糊 name/description |
| /api/scenics/{id} | GET | 景点详情 | id 路径 | {景点完整字段} | 不存在返回 404 |
| /api/scenics/map | GET | 地图简要列表 | 无 | [ {id,name,latitude,longitude,cover_image}, ... ] | 地图打点用 |

## 3. 商品与预订 Product / Booking
| 接口 | 方法 | 描述 | 查询参数 | data 结构 | 说明 |
|-------|------|------|----------|-----------|------|
| /api/products | GET | 商品列表(商城) | keyword,type,page,page_size | {list:[],page,page_size,total} | type 可 TRAVEL/HOTEL/TICKET |
| /api/products/{id} | GET | 商品详情 | id 路径 | {商品字段} | |
| /api/bookings | GET | 预订列表(酒店/门票) | type(必),city,page,page_size | {list:[],page,page_size,total} | type=HOTEL/TICKET; city 关联 scenic.city |

## 4. 行程计划 Trip Plan
| 接口 | 方法 | 描述 | 请求/查询参数 | data | 说明 |
|-------|------|------|-------------|------|------|
| /api/plans | POST | 创建/保存行程 | JSON: user_id,title,start_date,end_date,source,content | {plan_id} | content 建议 JSON 字符串 |
| /api/plans | GET | 用户行程列表 | user_id | [行程对象...] | 按 create_time DESC |
| /api/plans/{id} | GET | 行程详情 | id 路径 | {行程对象} | |

## 5. 收藏 Favorite
| 接口 | 方法 | 描述 | 参数 | data | 说明 |
|-------|------|------|------|------|------|
| /api/favorites | POST | 添加收藏 | JSON: user_id,target_id,target_type | null | 重复返回已收藏 |
| /api/favorites | DELETE | 取消收藏 | JSON: user_id,target_id,target_type | null | |
| /api/favorites/scenics | GET | 我的收藏景点 | user_id | [scenic...] | JOIN scenic |
| /api/favorites/products | GET | 我的收藏商品 | user_id | [product...] | JOIN product |

## 6. 去过 Visited
| 接口 | 方法 | 描述 | 参数 | data | 说明 |
|-------|------|------|------|------|------|
| /api/visited | POST | 标记去过 | JSON: user_id,scenic_id,rating | null | visit_date=当天 |
| /api/visited | GET | 我的去过列表 | user_id | [ {scenic_id,name,city,cover_image,visit_date,rating} ... ] | JOIN scenic |

## 7. 购物车 Cart
| 接口 | 方法 | 描述 | 参数 | data | 说明 |
|-------|------|------|------|------|------|
| /api/cart | POST | 加入购物车 | JSON: user_id,product_id,quantity | null | 已存在则数量累加 |
| /api/cart/{cart_id} | PUT | 修改数量 | JSON: quantity | null | quantity>0 |
| /api/cart/{cart_id} | DELETE | 删除条目 | 路径 cart_id | null | |
| /api/cart | GET | 查看购物车 | user_id | [ {cart_id,quantity,product:{...}} ... ] | product.type 可分组 |

## 8. 订单 Order
| 接口 | 方法 | 描述 | 参数 | data | 说明 |
|-------|------|------|------|------|------|
| /api/orders | POST | 创建订单(从购物车) | JSON: user_id,contact_name,contact_phone,order_type,checkin_date,checkout_date | {order_id,order_no} | 清空购物车 |
| /api/orders | GET | 用户订单列表 | user_id | [ {id,order_no,order_type,total_price,status,create_time,checkin_date,checkout_date} ... ] | DESC 排序 |
| /api/orders/{id} | GET | 订单详情 | id 路径 | {order:{...},items:[{product_id,name,cover_image,quantity,price,type}]} | JOIN product |

## 9. 错误码与说明
| code | 场景 | 说明 |
|------|------|------|
| 200 | 成功 | 正常业务返回 |
| 400 | 参数错误 | 例如缺少必填字段 |
| 401 | 认证失败 | 登录失败、密码错误 |
| 404 | 资源不存在 | 景点 / 商品 / 订单等未找到 |

## 10. 数据库表概要（与实现对应）
| 表 | 主要字段 | 说明 |
|----|----------|------|
| user | id, login_type, username, password, nickname, avatar_url, wx_openid | 用户及第三方标识 |
| scenic | id, name, city, cover_image, description, address, latitude, longitude, audio_url | 景点信息与坐标 |
| product | id, name, scenic_id, cover_image, price, stock, description, type, hotel_address | 商品 / 酒店 / 门票统一表 |
| favorite | id, user_id, target_id, target_type, create_time | 收藏记录 |
| visited | id, user_id, scenic_id, visit_date, rating | 去过记录与评分 |
| cart_item | id, user_id, product_id, quantity, create_time | 购物车条目 |
| order_main | id, order_no, user_id, order_type, total_price, status, create_time, contact_name, contact_phone, checkin_date, checkout_date | 订单主表 |
| order_item | id, order_id, product_id, quantity, price | 订单明细 |
| trip_plan | id, user_id, title, start_date, end_date, source, content, create_time | 行程计划 |

## 11. 典型调用序列示例
1. 注册/登录获取 user_id + token
2. 浏览景点 `/api/scenics` -> 加收藏 `/api/favorites`
3. 浏览商品 `/api/products` 或酒店 `/api/bookings` -> 加入购物车 `/api/cart`
4. 查看购物车 `/api/cart` -> 下单 `/api/orders`
5. 查看订单列表 `/api/orders` -> 查看订单详情 `/api/orders/{id}`
6. 标记去过 `/api/visited` 并查看足迹 `/api/visited`
7. 生成 AI 行程并保存 `/api/plans` -> 展示列表 `/api/plans`

## 12. 可扩展点建议
| 分类 | 建议 | 价值 |
|------|------|------|
| 安全 | 引入 JWT / token 校验中间件 | 防止未授权访问 |
| 性能 | 添加分页默认值校验 / SQL 索引补充 | 大数据量下更稳定 |
| 功能 | 天气接口 / 推荐接口 / 评论系统 | 增强用户体验 |
| 架构 | 拆分 Blueprint，增加 app factory | 代码模块化维护方便 |
| 数据 | 引入缓存 (Redis) 缓存热门景点 | 降低 DB 压力 |
| 日志 | 增加访问日志、错误日志 | 方便调试与运维 |

## 13. 快速运行与测试命令
```bash
# 安装依赖
pip install -r requirements.txt
# 运行服务
python app.py
# 健康检查
curl http://127.0.0.1:5000/ping
# 示例：注册
curl -X POST -H 'Content-Type: application/json' \
  -d '{"username":"u1","password":"123456"}' \
  http://127.0.0.1:5000/api/auth/register
```

## 14. 课程设计说明书撰写建议（摘要）
1. 选题背景：旅游出行应用需求增长，整合景点/商品/行程。\n2. 系统目标：提供统一后端支撑安卓端四大功能 Tab：首页/商城/地图/我的。\n3. 技术选型：Flask 简单轻量；SQLite 便于演示与移植；后续可升级。\n4. 数据库设计：九张核心业务表，满足用户行为与交易闭环。\n5. 接口设计：RESTful，统一返回结构，易于 Retrofit 对接。\n6. 安全设计：密码哈希处理；后续可加 JWT 与权限。\n7. 关键流程：购物车 -> 订单拆分写入；行程保存 JSON；收藏与去过简单关联。\n8. 性能与扩展：分页策略、索引使用；可引入缓存和搜索服务。\n9. 测试与验证：curl/Postman 验证接口；检查边界（空购物车、重复收藏、缺参错误）。\n10. 展望：加入实时推荐、地图热力图、第三方登录正式实现、消息通知等。

## 15. 冒烟测试 (Smoke Test) 与覆盖说明
本项目已编写脚本 `tests_smoke.py` 对所有接口进行一次端到端冒烟测试，确保核心路径与典型错误分支可用。

### 15.1 运行方式
```bash
python tests_smoke.py
```
运行后在控制台输出形如：
```
[PING] status=200 code=200 msg=OK
[REGISTER_OK] status=200 code=200 msg=注册成功
[REGISTER_DUP] status=400 code=400 msg=用户名已存在
...（中间省略）...
[CART_UPDATE_BAD] status=400 code=400 msg=quantity 必须大于 0
[ORDER_CART_EMPTY] status=400 code=400 msg=购物车为空
== 冒烟测试完成 ==
```

### 15.2 覆盖范围汇总
| 模块 | 成功场景 | 错误/边界场景 |
|------|----------|---------------|
| 健康检查 | /ping | - |
| 注册 | 正常注册 | 重复用户名 / 空用户名密码 |
| 登录 | 正确用户名密码 | 密码错误 / 空参数 |
| 微信登录 | 有 code 创建或复用用户 | 缺 code |
| 景点 | 列表(无筛选/keyword/city) / 详情存在 / 地图 | 详情不存在 |
| 商品 | 列表(泛 / type / keyword) / 详情存在 | 详情不存在 |
| 预订 | type=HOTEL&city / type=TICKET | 缺 type 参数 |
| 行程计划 | 创建 / 列表 / 详情存在 | 详情不存在 / 缺 user_id |
| 收藏 | 添加 scenic/product / 重复收藏 / 取消收藏 / 列表 | 缺必填字段（不在脚本中单测，可扩展） |
| 去过 | 添加记录 / 列表 | 缺 user_id |
| 购物车 | 加入多条 / 数量累加 / 修改数量>0 / 删除条目 / 列表 | 修改数量=0 / 缺必填字段（加入时） |
| 订单 | 创建 / 列表 / 详情存在 | 空购物车下单 / 详情不存在 / 缺 user_id |

### 15.3 重点验证点
1. 统一返回结构：所有接口均返回 {code,msg,data}，错误时 code !=200 且 HTTP 状态适配 (400/401/404)。
2. 分页接口：景点 / 商品 / 预订的 total 正常返回；可扩展增加 page/page_size 边界测试。
3. 业务约束：购物车 quantity 不接受 <=0；重复收藏不新增记录；空购物车禁止下单。
4. 关联 JOIN：收藏、订单、去过、预订接口的 JOIN 均在有无关联记录下表现正常（脚本通过存在/不存在场景验证）。
5. 错误分支：常见参数缺失 / 资源不存在均返回符合预期的错误码与 msg。

### 15.4 可追加测试建议
| 分类 | 建议测试场景 | 价值 |
|------|--------------|------|
| 分页 | page=0, page_size=负数/超大 | 参数校验健壮性 |
| 并发 | 同一商品多次快速加入购物车 | 累加逻辑正确性 |
| 边界 | 商品 stock=0 下单流程（需要增加库存校验） | 防止超卖 |
| 安全 | 模拟未登录访问（引入 token 后） | 权限控制验证 |
| 性能 | 大量景点/商品分页统计 | COUNT 子查询效率评估 |
| 数据一致性 | 下单后校验购物车已清空 | 事务完整性 |

### 15.5 如何扩展为自动化测试
1. 安装 pytest：`pip install pytest`
2. 将 `tests_smoke.py` 拆分为多个函数：`test_register_success` / `test_register_duplicate` 等。
3. 使用断言替换打印：`assert resp.status_code == 200`，`assert data['code'] == 200`。
4. 在 CI 中执行：`pytest -q`，结合覆盖率：`coverage run -m pytest && coverage report`。

### 15.6 质量基线
当前冒烟测试已证明：核心功能链条（注册 -> 收藏/购物车 -> 下单 -> 查看订单 / 去过 / 行程计划）可正常闭环，常见错误分支正确返回。适合作为后续迭代的最小安全网。

## 16. 下一步推荐实施清单（测试相关）
- 引入 pytest + coverage，量化覆盖率（目标 >70% 起步）。
- 增加分页与异常参数的系统化测试（边界值法）。
- 针对订单创建补充库存与并发安全测试（需要新增库存扣减逻辑）。
- 将随机用户名替换为 fixture 生成，提升可维护性。
- 引入 GitHub Actions / GitLab CI 在提交时自动跑测试。

---
文档至此结束，如需导出为 Word，可直接复制 Markdown 内容再排版。
