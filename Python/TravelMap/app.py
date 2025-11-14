# -*- coding: utf-8 -*-
"""
TravelMap 后端单文件实现 (Flask + SQLite)
==================================================
设计目的：课程设计演示一个旅游类 APP 后端，涵盖：用户 / 景点 / 商品 / 收藏 / 去过 / 行程计划 / 购物车 / 订单。

1. 技术栈：Flask + sqlite3 原生；不使用 ORM，便于理解 SQL 与业务映射。
2. 数据来源：直接使用既有 SQLite 文件 `db/TravelMap.sql`（已包含所有表结构与索引）。
3. 接口风格：RESTful，统一 JSON 返回结构：{"code":200,"msg":"OK","data":...}；错误时返回非 200 code 并附带提示。
4. 认证：本示例仅做简单 login（返回一个临时 UUID 作为 token，未做持久化），微信登录接口为占位演示 OAuth 流程。
5. 事务与并发：sqlite 在单用户本地开发场景足够；高并发需迁移至 MySQL/PostgreSQL 并加连接池。
6. 安全增强（后续可做）：
   - 密码哈希已有（Werkzeug），可加 salt 轮次配置。
   - 接口应校验 token（这里简化省略）。
   - 输入校验可引入 Marshmallow / Pydantic。
7. 目录当前为单文件，后续可拆分为 blueprint 模块：auth.py / scenic.py / product.py / order.py 等。

快速运行：
   pip install -r requirements.txt
   python app.py

课程说明书可以引用本文件中的中文注释段落（已分模块）。
"""

import os
import sqlite3
import uuid
from datetime import datetime

from flask import Flask, jsonify, request, g
from werkzeug.security import generate_password_hash, check_password_hash

# -------------------- 基础配置 --------------------
# BASE_DIR: 当前后端根目录；DB_PATH 指向已存在的 SQLite 数据库文件（不是 schema，而是数据文件）。
# 如果你后续要根据 schema 初始化一个新的库，可写一个 init_db 脚本：读取 schema.sql -> 新建 travel.db。
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
# 修改数据库文件名后缀为 .db（真实 SQLite 文件），避免把建表脚本 .sql 当数据库用
DB_PATH = os.path.join(BASE_DIR, 'db', 'TravelMap.db')  # 已存在的 SQLite 数据库


# 新增：启动前确保关键业务表存在（特别是 visited / cart_item，防止旧库缺表导致接口报错）
def ensure_schema():
    conn = sqlite3.connect(DB_PATH)
    conn.execute('PRAGMA foreign_keys = ON;')
    cur = conn.cursor()
    # visited 打卡记录
    cur.execute('''CREATE TABLE IF NOT EXISTS visited
                   (
                       id         INTEGER PRIMARY KEY AUTOINCREMENT,
                       user_id    INTEGER NOT NULL,
                       scenic_id  INTEGER NOT NULL,
                       visit_date TEXT,
                       rating     INTEGER,
                       FOREIGN KEY (user_id) REFERENCES user (id),
                       FOREIGN KEY (scenic_id) REFERENCES scenic (id)
                   );''')
    # cart_item 购物车
    cur.execute('''CREATE TABLE IF NOT EXISTS cart_item
                   (
                       id          INTEGER PRIMARY KEY AUTOINCREMENT,
                       user_id     INTEGER NOT NULL,
                       product_id  INTEGER NOT NULL,
                       quantity    INTEGER NOT NULL,
                       create_time TEXT,
                       FOREIGN KEY (user_id) REFERENCES user (id),
                       FOREIGN KEY (product_id) REFERENCES product (id)
                   );''')
    # favorite（已有时跳过）
    cur.execute('''CREATE TABLE IF NOT EXISTS favorite
                   (
                       id          INTEGER PRIMARY KEY AUTOINCREMENT,
                       user_id     INTEGER NOT NULL,
                       target_id   INTEGER NOT NULL,
                       target_type TEXT    NOT NULL,
                       create_time TEXT,
                       FOREIGN KEY (user_id) REFERENCES user (id)
                   );''')
    # trip_plan（已有时跳过）
    cur.execute('''CREATE TABLE IF NOT EXISTS trip_plan
                   (
                       id          INTEGER PRIMARY KEY AUTOINCREMENT,
                       user_id     INTEGER NOT NULL,
                       title       TEXT,
                       start_date  TEXT,
                       end_date    TEXT,
                       source      TEXT,
                       content     TEXT,
                       create_time TEXT,
                       FOREIGN KEY (user_id) REFERENCES user (id)
                   );''')
    conn.commit()
    conn.close()


# 保证启动前执行
ensure_schema()


# -------------------- 数据库工具函数 --------------------
# 说明：通过 Flask 的 g 对象为每个请求创建/缓存一个连接，结束时自动关闭；开启外键约束。
# row_factory 设置为 sqlite3.Row，便于通过列名访问字段。
def get_db():
    if 'db' not in g:
        conn = sqlite3.connect(DB_PATH)
        conn.row_factory = sqlite3.Row
        conn.execute('PRAGMA foreign_keys = ON;')
        g.db = conn
    return g.db


def close_db(e=None):
    db = g.pop('db', None)
    if db is not None:
        db.close()


# -------------------- Flask 应用与全局配置 --------------------
# JSON_AS_ASCII=False 保证返回中文不乱码。
# teardown_appcontext 注册数据库关闭逻辑。
app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False
app.teardown_appcontext(close_db)


# -------------------- 通用工具函数 --------------------
# json_response: 统一封装返回结构；http_status 与 code 区分，前端可统一按 code 判断业务成功与否。
# get_json: 简化 request.get_json() 的空值处理，避免 None。
def json_response(code=200, msg='OK', data=None, http_status=None):
    body = {
        'code': code,
        'msg': msg,
        'data': data,
    }
    if http_status is None:
        http_status = 200 if code == 200 else 400
    return jsonify(body), http_status


def get_json():
    if not request.is_json:
        return {}
    return request.get_json() or {}


# -------------------- 健康检查 --------------------
# 用于确认服务是否启动。可在部署后用于负载均衡健康探测。
# /ping -> {"code":200, "msg":"OK", "data":{"msg":"pong"}}
@app.route('/ping')
def ping():
    return json_response(data={'msg': 'pong'})


# =====================================================
# 一、用户模块 user（注册 / 登录 / 微信登录占位）
# =====================================================
# 表结构关键字段：id / login_type / username / password / nickname / avatar_url / wx_openid
# 注册时设置 login_type='LOCAL'；微信登录占位设置 login_type='WECHAT'。
# 密码使用 Werkzeug 提供的 generate_password_hash + check_password_hash。


@app.route('/api/auth/register', methods=['POST'])
def register():
    data = get_json()
    username = data.get('username', '').strip()
    password = data.get('password', '').strip()
    phone = data.get('phone')
    email = data.get('email')
    nickname = data.get('nickname')

    if not username or not password:
        return json_response(400, '用户名和密码不能为空', None, 400)

    db = get_db()
    # 唯一性检查
    cur = db.execute('SELECT id FROM user WHERE username = ?', (username,))
    if cur.fetchone():
        return json_response(400, '用户名已存在', None, 400)

    # 密码加密存储
    pwd_hash = generate_password_hash(password)
    cur = db.execute(
        'INSERT INTO user (login_type, username, password, phone, email, nickname) '
        'VALUES (?, ?, ?, ?, ?, ?)',
        ('LOCAL', username, pwd_hash, phone, email, nickname)
    )
    db.commit()
    user_id = cur.lastrowid
    return json_response(200, '注册成功', {'user_id': user_id})


@app.route('/api/auth/login', methods=['POST'])
def login():
    data = get_json()
    username = data.get('username', '').strip()
    password = data.get('password', '').strip()

    if not username or not password:
        return json_response(400, '用户名和密码不能为空', None, 400)

    db = get_db()
    cur = db.execute(
        'SELECT id, password, nickname, avatar_url FROM user WHERE username = ?',
        (username,)
    )
    row = cur.fetchone()
    if row is None or not check_password_hash(row['password'], password):
        return json_response(401, '用户名或密码错误', None, 401)

    # 生成临时 token（可扩展为 JWT 或数据库持久化 session）
    token = uuid.uuid4().hex
    data = {
        'user_id': row['id'],
        'nickname': row['nickname'],
        'avatar_url': row['avatar_url'],
        'token': token,
    }
    return json_response(200, '登录成功', data)


@app.route('/api/auth/wechat', methods=['POST'])
def wechat_login():
    """微信登录占位：模拟 OAuth 流程；真实实现应使用 code 换取 access_token / openid 后再建用户。"""
    data = get_json()
    code = data.get('code')
    if not code:
        return json_response(400, 'code 不能为空', None, 400)

    db = get_db()
    # 用 code 伪造 openid（演示用）
    mock_openid = f'mock_openid_{code}'

    cur = db.execute('SELECT * FROM user WHERE wx_openid = ?', (mock_openid,))
    row = cur.fetchone()
    if row is None:
        nickname = f'微信用户_{code[-4:]}'
        cur = db.execute(
            'INSERT INTO user (login_type, nickname, wx_openid) VALUES (?, ?, ?)',
            ('WECHAT', nickname, mock_openid)
        )
        db.commit()
        user_id = cur.lastrowid
        avatar_url = None
    else:
        user_id = row['id']
        nickname = row['nickname']
        avatar_url = row['avatar_url']

    token = uuid.uuid4().hex
    return json_response(200, '微信登录成功', {
        'user_id': user_id,
        'nickname': nickname,
        'avatar_url': avatar_url,
        'token': token,
    })


# =====================================================
# 二、景点模块 scenic（列表 / 搜索 / 详情 / 地图）
# =====================================================
# 支持 keyword 模糊匹配 name + description，city 精确匹配；附带分页。
# 地图接口返回精简字段用于前端标点。


@app.route('/api/scenics', methods=['GET'])
def scenic_list():
    keyword = request.args.get('keyword', '').strip()
    city = request.args.get('city', '').strip()
    page = int(request.args.get('page', 1) or 1)
    page_size = int(request.args.get('page_size', 10) or 10)

    db = get_db()
    sql = 'SELECT * FROM scenic WHERE 1=1'
    params = []

    if keyword:
        sql += ' AND (name LIKE ? OR description LIKE ?)'
        kw = f'%{keyword}%'
        params.extend([kw, kw])
    if city:
        sql += ' AND city = ?'
        params.append(city)

    # 统计总数（子查询包裹防止分页影响）
    count_sql = 'SELECT COUNT(1) AS cnt FROM (' + sql + ') AS t'
    cur = db.execute(count_sql, params)
    total = cur.fetchone()['cnt']

    offset = (page - 1) * page_size
    sql += ' LIMIT ? OFFSET ?'
    params_with_page = params + [page_size, offset]

    cur = db.execute(sql, params_with_page)
    rows = [dict(r) for r in cur.fetchall()]

    return json_response(200, 'OK', {
        'list': rows,
        'page': page,
        'page_size': page_size,
        'total': total,
    })


@app.route('/api/scenics/<int:sid>', methods=['GET'])
def scenic_detail(sid):
    db = get_db()
    cur = db.execute('SELECT * FROM scenic WHERE id = ?', (sid,))
    row = cur.fetchone()
    if row is None:
        return json_response(404, '景点不存在', None, 404)
    return json_response(200, 'OK', dict(row))


@app.route('/api/scenics/map', methods=['GET'])
def scenic_map():
    db = get_db()
    cur = db.execute('SELECT id, name, latitude, longitude, cover_image FROM scenic')
    rows = [dict(r) for r in cur.fetchall()]
    return json_response(200, 'OK', rows)


# =====================================================
# 三、商品与预订 product（商品列表 / 详情 / 酒店门票筛选）
# =====================================================
# product.type 业务含义：TRAVEL 周边 / HOTEL 酒店 / TICKET 门票。
# /api/products 用于商城泛查询；/api/bookings 聚焦 HOTEL/TICKET 并可按城市过滤。


@app.route('/api/products', methods=['GET'])
def product_list():
    keyword = request.args.get('keyword', '').strip()
    ptype = request.args.get('type', '').strip()
    page = int(request.args.get('page', 1) or 1)
    page_size = int(request.args.get('page_size', 10) or 10)

    db = get_db()
    sql = 'SELECT * FROM product WHERE 1=1'
    params = []

    if keyword:
        sql += ' AND (name LIKE ? OR description LIKE ?)'
        kw = f'%{keyword}%'
        params.extend([kw, kw])
    if ptype:
        sql += ' AND type = ?'
        params.append(ptype)

    count_sql = 'SELECT COUNT(1) AS cnt FROM (' + sql + ') AS t'
    cur = db.execute(count_sql, params)
    total = cur.fetchone()['cnt']

    offset = (page - 1) * page_size
    sql += ' LIMIT ? OFFSET ?'
    params_with_page = params + [page_size, offset]

    cur = db.execute(sql, params_with_page)
    rows = [dict(r) for r in cur.fetchall()]

    return json_response(200, 'OK', {
        'list': rows,
        'page': page,
        'page_size': page_size,
        'total': total,
    })


@app.route('/api/products/<int:pid>', methods=['GET'])
def product_detail(pid):
    db = get_db()
    cur = db.execute('SELECT * FROM product WHERE id = ?', (pid,))
    row = cur.fetchone()
    if row is None:
        return json_response(404, '商品不存在', None, 404)
    return json_response(200, 'OK', dict(row))


@app.route('/api/bookings', methods=['GET'])
def booking_list():
    btype = request.args.get('type', '').strip()  # HOTEL / TICKET
    city = request.args.get('city', '').strip()
    page = int(request.args.get('page', 1) or 1)
    page_size = int(request.args.get('page_size', 10) or 10)

    if not btype:
        return json_response(400, 'type 参数必填(HOTEL/TICKET)', None, 400)

    db = get_db()
    sql = 'SELECT p.* FROM product p'
    params = []
    if city:
        sql += ' LEFT JOIN scenic s ON p.scenic_id = s.id'
    sql += ' WHERE p.type = ?'
    params.append(btype)
    if city:
        sql += ' AND s.city = ?'
        params.append(city)

    count_sql = 'SELECT COUNT(1) AS cnt FROM (' + sql + ') AS t'
    cur = db.execute(count_sql, params)
    total = cur.fetchone()['cnt']

    offset = (page - 1) * page_size
    sql += ' LIMIT ? OFFSET ?'
    params_with_page = params + [page_size, offset]
    cur = db.execute(sql, params_with_page)
    rows = [dict(r) for r in cur.fetchall()]

    return json_response(200, 'OK', {
        'list': rows,
        'page': page,
        'page_size': page_size,
        'total': total,
    })


# =====================================================
# 四、行程计划 trip_plan（保存 / 列表 / 详情）
# =====================================================
# content 字段建议存放结构化 JSON 字符串，前端可进一步解析展示天数安排。
# create_time 使用应用层写入 YYYY-MM-DD HH:MM:SS 方便排序。


@app.route('/api/plans', methods=['POST'])
def create_plan():
    data = get_json()
    user_id = data.get('user_id')
    title = data.get('title')
    start_date = data.get('start_date')
    end_date = data.get('end_date')
    source = data.get('source', 'AI')
    content = data.get('content')

    if not user_id:
        return json_response(400, 'user_id 必填', None, 400)

    db = get_db()
    cur = db.execute(
        'INSERT INTO trip_plan (user_id, title, start_date, end_date, source, content, create_time) '
        'VALUES (?, ?, ?, ?, ?, ?, ?)',
        (user_id, title, start_date, end_date, source, content, datetime.now().strftime('%Y-%m-%d %H:%M:%S'))
    )
    db.commit()
    plan_id = cur.lastrowid
    return json_response(200, '保存成功', {'plan_id': plan_id})


@app.route('/api/plans', methods=['GET'])
def list_plans():
    user_id = request.args.get('user_id')
    if not user_id:
        return json_response(400, 'user_id 必填', None, 400)

    db = get_db()
    cur = db.execute('SELECT * FROM trip_plan WHERE user_id = ? ORDER BY create_time DESC', (user_id,))
    rows = [dict(r) for r in cur.fetchall()]
    return json_response(200, 'OK', rows)


@app.route('/api/plans/<int:pid>', methods=['GET'])
def plan_detail(pid):
    db = get_db()
    cur = db.execute('SELECT * FROM trip_plan WHERE id = ?', (pid,))
    row = cur.fetchone()
    if row is None:
        return json_response(404, '行程不存在', None, 404)
    return json_response(200, 'OK', dict(row))


# =====================================================
# 五、收藏 favorite（添加 / 删除 / 列表）
# =====================================================
# favorite 设计：user_id + target_id + target_type(SCENIC|PRODUCT)；不做去重约束但代码层防重复插入。


@app.route('/api/favorites', methods=['POST'])
def add_favorite():
    data = get_json()
    user_id = data.get('user_id')
    target_id = data.get('target_id')
    target_type = data.get('target_type')

    if not all([user_id, target_id, target_type]):
        return json_response(400, 'user_id/target_id/target_type 必填', None, 400)

    db = get_db()
    cur = db.execute('SELECT id FROM favorite WHERE user_id = ? AND target_id = ? AND target_type = ?',
                     (user_id, target_id, target_type))
    if cur.fetchone():
        return json_response(200, '已收藏', None, 200)

    db.execute('INSERT INTO favorite (user_id, target_id, target_type, create_time) VALUES (?, ?, ?, ?)',
               (user_id, target_id, target_type, datetime.now().strftime('%Y-%m-%d %H:%M:%S')))
    db.commit()
    return json_response(200, '收藏成功', None)


@app.route('/api/favorites', methods=['DELETE'])
def remove_favorite():
    data = get_json()
    user_id = data.get('user_id')
    target_id = data.get('target_id')
    target_type = data.get('target_type')

    if not all([user_id, target_id, target_type]):
        return json_response(400, 'user_id/target_id/target_type 必填', None, 400)

    db = get_db()
    db.execute('DELETE FROM favorite WHERE user_id = ? AND target_id = ? AND target_type = ?',
               (user_id, target_id, target_type))
    db.commit()
    return json_response(200, '已取消收藏', None)


@app.route('/api/favorites/scenics', methods=['GET'])
def my_fav_scenics():
    user_id = request.args.get('user_id')
    if not user_id:
        return json_response(400, 'user_id 必填', None, 400)

    db = get_db()
    sql = 'SELECT s.* FROM favorite f JOIN scenic s ON f.target_id = s.id WHERE f.user_id = ? AND f.target_type = "SCENIC"'
    cur = db.execute(sql, (user_id,))
    rows = [dict(r) for r in cur.fetchall()]
    return json_response(200, 'OK', rows)


@app.route('/api/favorites/products', methods=['GET'])
def my_fav_products():
    user_id = request.args.get('user_id')
    if not user_id:
        return json_response(400, 'user_id 必填', None, 400)

    db = get_db()
    sql = 'SELECT p.* FROM favorite f JOIN product p ON f.target_id = p.id WHERE f.user_id = ? AND f.target_type = "PRODUCT"'
    cur = db.execute(sql, (user_id,))
    rows = [dict(r) for r in cur.fetchall()]
    return json_response(200, 'OK', rows)


# =====================================================
# 六、去过 visited（打卡记录）
# =====================================================
# visited: 记录用户去过的景点与评分；visit_date 使用当天日期；可用于生成用户足迹地图。


@app.route('/api/visited', methods=['POST'])
def add_visited():
    data = get_json()
    user_id = data.get('user_id')
    scenic_id = data.get('scenic_id')
    rating = data.get('rating')

    if not all([user_id, scenic_id]):
        return json_response(400, 'user_id/scenic_id 必填', None, 400)

    db = get_db()
    db.execute('INSERT INTO visited (user_id, scenic_id, visit_date, rating) VALUES (?, ?, ?, ?)',
               (user_id, scenic_id, datetime.now().strftime('%Y-%m-%d'), rating))
    db.commit()
    return json_response(200, '已标记为去过', None)


@app.route('/api/visited', methods=['GET'])
def list_visited():
    user_id = request.args.get('user_id')
    if not user_id:
        return json_response(400, 'user_id 必填', None, 400)

    db = get_db()
    sql = 'SELECT v.scenic_id, v.visit_date, v.rating, s.name, s.city, s.cover_image FROM visited v JOIN scenic s ON v.scenic_id = s.id WHERE v.user_id = ? ORDER BY v.visit_date DESC'
    cur = db.execute(sql, (user_id,))
    rows = [dict(r) for r in cur.fetchall()]
    return json_response(200, 'OK', rows)


# =====================================================
# 七、购物车 cart_item（添加 / 修改 / 删除 / 列表）
# =====================================================
# 设计说明：不区分商品类型放同一购物车；前端按 product.type 对列表做分组展示。
# 修改数量时若设为 0 可按业务需要改成删除，这里简化为数量必须 >0。


@app.route('/api/cart', methods=['POST'])
def add_cart_item():
    data = get_json()
    user_id = data.get('user_id')
    product_id = data.get('product_id')
    # 不再使用 (value or 1) 以免将 0 错误提升为 1
    raw_q = data.get('quantity')
    quantity = 1 if raw_q is None else int(raw_q)
    if not all([user_id, product_id]):
        return json_response(400, 'user_id/product_id 必填', None, 400)
    if quantity <= 0:
        return json_response(400, 'quantity 必须大于 0', None, 400)
    db = get_db()
    cur = db.execute('SELECT id, quantity FROM cart_item WHERE user_id = ? AND product_id = ?', (user_id, product_id))
    row = cur.fetchone()
    if row:
        new_q = row['quantity'] + quantity
        db.execute('UPDATE cart_item SET quantity = ? WHERE id = ?', (new_q, row['id']))
    else:
        db.execute('INSERT INTO cart_item (user_id, product_id, quantity, create_time) VALUES (?, ?, ?, ?)',
                   (user_id, product_id, quantity, datetime.now().strftime('%Y-%m-%d %H:%M:%S')))
    db.commit()
    return json_response(200, '加入购物车成功', None)


@app.route('/api/cart/<int:cart_id>', methods=['PUT'])
def update_cart_item(cart_id):
    data = get_json()
    raw_q = data.get('quantity')
    quantity = 1 if raw_q is None else int(raw_q)
    if quantity <= 0:
        return json_response(400, 'quantity 必须大于 0', None, 400)
    db = get_db()
    db.execute('UPDATE cart_item SET quantity = ? WHERE id = ?', (quantity, cart_id))
    db.commit()
    return json_response(200, '修改成功', None)


@app.route('/api/cart/<int:cart_id>', methods=['DELETE'])
def delete_cart_item(cart_id):
    db = get_db()
    db.execute('DELETE FROM cart_item WHERE id = ?', (cart_id,))
    db.commit()
    return json_response(200, '删除成功', None)


@app.route('/api/cart', methods=['GET'])
def list_cart():
    user_id = request.args.get('user_id')
    if not user_id:
        return json_response(400, 'user_id 必填', None, 400)

    db = get_db()
    sql = 'SELECT c.id AS cart_id, c.quantity, p.id AS product_id, p.name, p.cover_image, p.price, p.type, p.hotel_address FROM cart_item c JOIN product p ON c.product_id = p.id WHERE c.user_id = ?'
    cur = db.execute(sql, (user_id,))
    items = []
    for row in cur.fetchall():
        row = dict(row)
        items.append({'cart_id': row['cart_id'], 'quantity': row['quantity'],
                      'product': {'id': row['product_id'], 'name': row['name'], 'cover_image': row['cover_image'],
                                  'price': row['price'], 'type': row['type'], 'hotel_address': row['hotel_address']}})
    return json_response(200, 'OK', items)


# =====================================================
# 八、订单 order_main + order_item（创建 / 列表 / 详情）
# =====================================================
# 创建订单流程：读取购物车 -> 计算总价 -> 写 order_main -> 写多条 order_item -> 清空购物车。
# order_type 用于酒店订单区分（HOTEL）或普通商品（PRODUCT / TICKET）。
# 订单号简单使用时间戳 + UUID 片段，实际可改为更规范的规则（日期+自增）。


@app.route('/api/orders', methods=['POST'])
def create_order():
    data = get_json()
    user_id = data.get('user_id')
    contact_name = data.get('contact_name')
    contact_phone = data.get('contact_phone')
    order_type = data.get('order_type', 'PRODUCT')
    checkin_date = data.get('checkin_date')
    checkout_date = data.get('checkout_date')

    if not user_id:
        return json_response(400, 'user_id 必填', None, 400)

    db = get_db()
    cur = db.execute(
        'SELECT c.product_id, c.quantity, p.price FROM cart_item c JOIN product p ON c.product_id = p.id WHERE c.user_id = ?',
        (user_id,))
    items = cur.fetchall()
    if not items:
        return json_response(400, '购物车为空', None, 400)

    total_price = sum(row['price'] * row['quantity'] for row in items)
    order_no = datetime.now().strftime('%Y%m%d%H%M%S') + uuid.uuid4().hex[:6]

    cur = db.execute(
        'INSERT INTO order_main (order_no, user_id, order_type, total_price, status, create_time, contact_name, contact_phone, checkin_date, checkout_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
        (order_no, user_id, order_type, total_price, 'CREATED', datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
         contact_name, contact_phone, checkin_date, checkout_date))
    order_id = cur.lastrowid

    for row in items:
        db.execute('INSERT INTO order_item (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)',
                   (order_id, row['product_id'], row['quantity'], row['price']))

    db.execute('DELETE FROM cart_item WHERE user_id = ?', (user_id,))
    db.commit()

    return json_response(200, '下单成功', {'order_id': order_id, 'order_no': order_no})


@app.route('/api/orders', methods=['GET'])
def list_orders():
    user_id = request.args.get('user_id')
    if not user_id:
        return json_response(400, 'user_id 必填', None, 400)

    db = get_db()
    sql = 'SELECT id, order_no, order_type, total_price, status, create_time, checkin_date, checkout_date FROM order_main WHERE user_id = ? ORDER BY create_time DESC'
    cur = db.execute(sql, (user_id,))
    rows = [dict(r) for r in cur.fetchall()]
    return json_response(200, 'OK', rows)


@app.route('/api/orders/<int:oid>', methods=['GET'])
def order_detail(oid):
    db = get_db()
    cur = db.execute('SELECT * FROM order_main WHERE id = ?', (oid,))
    order = cur.fetchone()
    if order is None:
        return json_response(404, '订单不存在', None, 404)

    cur = db.execute(
        'SELECT oi.product_id, oi.quantity, oi.price, p.name, p.cover_image, p.type FROM order_item oi JOIN product p ON oi.product_id = p.id WHERE oi.order_id = ?',
        (oid,))
    items = []
    for row in cur.fetchall():
        row = dict(row)
        items.append({'product_id': row['product_id'], 'name': row['name'], 'cover_image': row['cover_image'],
                      'quantity': row['quantity'], 'price': row['price'], 'type': row['type']})

    return json_response(200, 'OK', {'order': dict(order), 'items': items})


# =====================================================
# 主入口 main
# =====================================================
# debug=True 便于开发查看错误堆栈；生产环境建议关闭并使用 gunicorn / waitress 等 WSGI 部署。
# host=0.0.0.0 允许局域网设备（手机 / 模拟器）访问。
if __name__ == '__main__':
    # 端口改为 5001 避免本机 5000 已被占用
    app.run(host='0.0.0.0', port=5001, debug=True)
