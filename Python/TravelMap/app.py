# -*- coding: utf-8 -*-
"""
TravelMap 后端单文件实现 (Flask + SQLite)
==================================================
设计目的：课程设计演示一个旅游类 APP 后端，涵盖：用户 / 景点 / 商品 / 收藏 / 去过 / 行程计划 / 购物车 / 订单。

1. 技术栈：Flask + sqlite3 原生；不使用 ORM，便于理解 SQL 与业务映射。
2. 数据来源：直接使用既有 SQLite 文件 `db/TravelMap.sql`（已包含所有表结构与索引）。
3. 接口风格：RESTful，统一 JSON 返回结构：{"code":200,"msg":"OK","data":...}；错误时返回非 200 code 并附带提示。
4. 认证：本示例仅做简单 login，返回完整用户资料，不发放 token；微信登录接口为占位演示 OAuth 流程。
5. 事务与并发：sqlite 在单用户本地开发场景足够；高并发需迁移至 MySQL/PostgreSQL 并加连接池。
6. 安全增强（后续可做）：
   - 密码哈希已有（Werkzeug），可加 salt 轮次配置。
   - 接口如需鉴权可扩展 session/JWT 模块（当前示例未启用 token）。
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
import hashlib
from datetime import datetime, timedelta
from functools import lru_cache
from urllib.parse import quote_plus

from flask import Flask, jsonify, request, g, render_template, send_from_directory, abort, url_for
from werkzeug.security import generate_password_hash, check_password_hash

import requests
from requests import RequestException

# -------------------- 基础配置 --------------------
# BASE_DIR: 当前后端根目录；DB_PATH 指向已存在的 SQLite 数据库文件（不是 schema，而是数据文件）。
# 如果你后续要根据 schema 初始化一个新的库，可写一个 init_db 脚本：读取 schema.sql -> 新建 travel.db。
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
# 修改数据库文件名后缀为 .db（真实 SQLite 文件），避免把建表脚本 .sql 当数据库用
DB_PATH = os.path.join(BASE_DIR, 'db', 'TravelMap.db')  # 已存在的 SQLite 数据库
DOC_DIR = os.path.join(BASE_DIR, 'doc')
GITHUB_URL = 'https://github.com/Justtyn/TravelMap'
APK_FILENAME = 'TravelMap.apk'
ANDROID_VERSION = '0.9.2-beta'

WECHAT_APP_ID = 'wxb47bc8f618cc1b59'
WECHAT_APP_SECRET = '84ae2dde3996c26339ad06c7c55345a8'
WECHAT_OAUTH_URL = os.environ.get(
    'WECHAT_OAUTH_URL',
    'https://api.weixin.qq.com/sns/oauth2/access_token'
)
WECHAT_USERINFO_URL = os.environ.get(
    'WECHAT_USERINFO_URL',
    'https://api.weixin.qq.com/sns/userinfo'
)
try:
    WECHAT_HTTP_TIMEOUT = float(os.environ.get('WECHAT_HTTP_TIMEOUT', '5'))
except ValueError:
    WECHAT_HTTP_TIMEOUT = 5.0


def human_readable_size(num_bytes):
    units = ['B', 'KB', 'MB', 'GB']
    value = float(num_bytes)
    for unit in units:
        if value < 1024 or unit == units[-1]:
            if unit == 'B':
                return f"{int(value)} {unit}"
            return f"{value:.1f} {unit}"
        value /= 1024


@lru_cache(maxsize=1)
def get_apk_metadata():
    apk_path = os.path.join(BASE_DIR, 'static', APK_FILENAME)
    if not os.path.isfile(apk_path):
        return {
            'filename': APK_FILENAME,
            'version': ANDROID_VERSION,
            'updated_at': '暂无记录',
            'size': '未知',
            'sha256': '文件缺失'
        }

    stat = os.stat(apk_path)
    with open(apk_path, 'rb') as fh:
        sha256 = hashlib.sha256(fh.read()).hexdigest()
    return {
        'filename': APK_FILENAME,
        'version': ANDROID_VERSION,
        'updated_at': datetime.fromtimestamp(stat.st_mtime).strftime('%Y-%m-%d %H:%M'),
        'size': human_readable_size(stat.st_size),
        'sha256': sha256
    }


API_SECTIONS = [
    {
        'title': '认证 / 用户',
        'description': '注册 / 登录 / 访客信息等接口，为 Demo 提供最基本的账号体系。',
        'endpoints': [
            {
                'name': '注册账号',
                'method': 'POST',
                'path': '/api/auth/register',
                'summary': '用户名 + 密码快速注册本地账号。',
                'requires_auth': False,
                'params': [
                    {'name': 'username', 'type': 'string', 'required': True, 'desc': '唯一用户名'},
                    {'name': 'password', 'type': 'string', 'required': True, 'desc': '明文密码（示例环境）'},
                    {'name': 'phone', 'type': 'string', 'required': False, 'desc': '手机号，可选'},
                    {'name': 'email', 'type': 'string', 'required': False, 'desc': '邮箱，可选'}
                ],
                'response': {'code': 200, 'msg': 'OK', 'data': {'id': 1, 'username': 'demo'}} ,
                'sample_body': {'username': 'demo_user', 'password': '123456'}
            },
            {
                'name': '账号登录',
                'method': 'POST',
                'path': '/api/auth/login',
                'summary': '输入用户名/密码返回用户资料。',
                'requires_auth': False,
                'params': [
                    {'name': 'username', 'type': 'string', 'required': True, 'desc': '已注册用户名'},
                    {'name': 'password', 'type': 'string', 'required': True, 'desc': '登录密码'}
                ],
                'response': {'code': 200, 'data': {'id': 1, 'nickname': '旅图'}},
                'sample_body': {'username': 'demo_user', 'password': '123456'}
            },
            {
                'name': '更新资料',
                'method': 'POST',
                'path': '/api/user/update',
                'summary': '更改昵称、头像等资料字段。',
                'requires_auth': True,
                'params': [
                    {'name': 'user_id', 'type': 'number', 'required': True, 'desc': '用户 ID'},
                    {'name': 'nickname', 'type': 'string', 'required': False, 'desc': '昵称'},
                    {'name': 'avatar_url', 'type': 'string', 'required': False, 'desc': '头像地址'}
                ],
                'response': {'code': 200, 'msg': 'OK'},
                'sample_body': {'user_id': 1, 'nickname': '旅友'}
            }
        ]
    },
    {
        'title': '内容 / 商品',
        'description': '景点列表、商品与收藏行为 API，覆盖灵感流与商城。',
        'endpoints': [
            {
                'name': '景点列表',
                'method': 'GET',
                'path': '/api/scenic/list',
                'summary': '分页返回景点卡片，支持城市 / 关键字过滤。',
                'requires_auth': False,
                'params': [
                    {'name': 'page', 'type': 'number', 'required': False, 'desc': '页码，默认 1'},
                    {'name': 'size', 'type': 'number', 'required': False, 'desc': '每页数量，默认 10'},
                    {'name': 'city', 'type': 'string', 'required': False, 'desc': '按城市筛选'},
                    {'name': 'keyword', 'type': 'string', 'required': False, 'desc': '模糊搜索'}
                ],
                'response': {'code': 200, 'data': {'items': '[]', 'total': 120}},
                'sample_query': 'page=1&size=10'
            },
            {
                'name': '商品详情',
                'method': 'GET',
                'path': '/api/product/detail',
                'summary': '根据商品 ID 返回库存 / 价格 / 所属景点。',
                'requires_auth': False,
                'params': [
                    {'name': 'product_id', 'type': 'number', 'required': True, 'desc': '商品 ID'}
                ],
                'response': {'code': 200, 'data': {'id': 5, 'stock': 8}},
                'sample_query': 'product_id=1'
            },
            {
                'name': '收藏 / 取消',
                'method': 'POST',
                'path': '/api/favorite/toggle',
                'summary': '收藏或取消收藏景点/商品，自动判断目标类型。',
                'requires_auth': True,
                'params': [
                    {'name': 'user_id', 'type': 'number', 'required': True, 'desc': '用户 ID'},
                    {'name': 'target_id', 'type': 'number', 'required': True, 'desc': '目标 ID'},
                    {'name': 'target_type', 'type': 'enum', 'required': True, 'desc': 'SCENIC / PRODUCT'}
                ],
                'response': {'code': 200, 'msg': 'OK'},
                'sample_body': {'user_id': 1, 'target_id': 2, 'target_type': 'SCENIC'}
            }
        ]
    },
    {
        'title': '订单 / 行程',
        'description': '购物车、订单与行程计划接口，复现交易闭环。',
        'endpoints': [
            {
                'name': '购物车列表',
                'method': 'GET',
                'path': '/api/cart/list',
                'summary': '返回用户购物车条目及商品详情。',
                'requires_auth': True,
                'params': [
                    {'name': 'user_id', 'type': 'number', 'required': True, 'desc': '用户 ID'}
                ],
                'response': {'code': 200, 'data': {'items': '[]'}},
                'sample_query': 'user_id=1'
            },
            {
                'name': '创建订单',
                'method': 'POST',
                'path': '/api/order/create',
                'summary': '提交购物车条目生成订单，返回订单号。',
                'requires_auth': True,
                'params': [
                    {'name': 'user_id', 'type': 'number', 'required': True, 'desc': '用户 ID'},
                    {'name': 'items', 'type': 'array', 'required': True, 'desc': '商品项 ID 列表'}
                ],
                'response': {'code': 200, 'data': {'order_no': 'T2024001'}},
                'sample_body': {'user_id': 1, 'items': [1, 2]}
            },
            {
                'name': '行程计划',
                'method': 'GET',
                'path': '/api/plan/list',
                'summary': '列出 trip_plan，展示时间区间与内容。',
                'requires_auth': True,
                'params': [
                    {'name': 'user_id', 'type': 'number', 'required': True, 'desc': '用户 ID'}
                ],
                'response': {'code': 200, 'data': {'plans': '[]'}},
                'sample_query': 'user_id=1'
            }
        ]
    }
]


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


class WeChatConfigError(Exception):
    """Raised when mandatory WeChat config is missing."""


class WeChatAPIError(Exception):
    """Raised when calling the WeChat Open Platform fails."""

    def __init__(self, errcode, errmsg):
        super().__init__(f'WeChat API error {errcode}: {errmsg}')
        self.errcode = errcode
        self.errmsg = errmsg


def _ensure_wechat_env():
    if not WECHAT_APP_ID or not WECHAT_APP_SECRET:
        raise WeChatConfigError('WECHAT_APP_ID/WECHAT_APP_SECRET 未配置')
    return WECHAT_APP_ID, WECHAT_APP_SECRET


def exchange_code_for_wechat_token(code):
    app_id, app_secret = _ensure_wechat_env()
    params = {
        'appid': app_id,
        'secret': app_secret,
        'code': code,
        'grant_type': 'authorization_code'
    }
    try:
        resp = requests.get(WECHAT_OAUTH_URL, params=params, timeout=WECHAT_HTTP_TIMEOUT)
    except RequestException as exc:
        raise WeChatAPIError(-1, f'网络异常：{exc}') from exc
    try:
        payload = resp.json()
    except ValueError as exc:
        raise WeChatAPIError(-2, '解析微信响应失败') from exc

    errcode = payload.get('errcode')
    if errcode:
        raise WeChatAPIError(errcode, payload.get('errmsg', '微信接口返回错误'))
    if 'openid' not in payload:
        raise WeChatAPIError(-3, '微信响应缺少 openid')
    return payload


def fetch_wechat_user_profile(access_token, openid):
    if not access_token or not openid:
        return {}
    params = {
        'access_token': access_token,
        'openid': openid,
        'lang': 'zh_CN'
    }
    try:
        resp = requests.get(WECHAT_USERINFO_URL, params=params, timeout=WECHAT_HTTP_TIMEOUT)
        data = resp.json()
    except (RequestException, ValueError):
        return {}
    if data.get('errcode'):
        return {}
    return data


def compute_wechat_token_expire_at(expires_in):
    try:
        seconds = int(expires_in)
    except (TypeError, ValueError):
        return None
    expire_at = datetime.utcnow() + timedelta(seconds=seconds)
    return expire_at.strftime('%Y-%m-%d %H:%M:%S')


def get_json():
    if not request.is_json:
        return {}
    return request.get_json() or {}


def normalize_optional_str(value):
    """Trim optional string fields; empty string becomes None."""
    if value is None:
        return None
    if isinstance(value, str):
        trimmed = value.strip()
        return trimmed or None
    return value


PRODUCT_COLUMNS = ['id', 'name', 'scenic_id', 'cover_image', 'price', 'stock', 'description', 'type', 'hotel_address']
SCENIC_COLUMNS = ['id', 'name', 'city', 'cover_image', 'description', 'address', 'latitude', 'longitude', 'audio_url']
PRODUCT_SELECT_COLUMNS = ', '.join([f'p.{col} AS product_{col}' for col in PRODUCT_COLUMNS])
SCENIC_SELECT_COLUMNS = ', '.join([f's.{col} AS scenic_{col}' for col in SCENIC_COLUMNS])


def row_to_dict(row):
    return dict(row) if row is not None else None


def sanitize_user_row(row):
    data = row_to_dict(row)
    if not data:
        return None
    data.pop('password', None)
    return data


def extract_prefixed_fields(row, prefix):
    data = {}
    if row is None:
        return data
    for key in row.keys():
        if key.startswith(prefix):
            data[key[len(prefix):]] = row[key]
    if data and all(value is None for value in data.values()):
        return {}
    return data


def build_cart_payload(row):
    if row is None:
        return None
    product = extract_prefixed_fields(row, 'product_')
    return {
        'cart_id': row['cart_id'],
        'user_id': row['user_id'],
        'quantity': row['quantity'],
        'create_time': row['create_time'],
        'product': product
    }


def build_visited_payload(row):
    if row is None:
        return None
    scenic = extract_prefixed_fields(row, 'scenic_')
    return {
        'visited_id': row['id'],
        'user_id': row['user_id'],
        'scenic_id': row['scenic_id'],
        'visit_date': row['visit_date'],
        'rating': row['rating'],
        'scenic': scenic or None,
    }


def build_order_item_payload(row):
    if row is None:
        return None
    product = extract_prefixed_fields(row, 'product_')
    return {
        'order_item_id': row['order_item_id'],
        'order_id': row['order_id'],
        'product_id': row['product_id'],
        'quantity': row['quantity'],
        'price': row['price'],
        'product': product or None,
    }


def fetch_order_items_map(order_ids):
    if not order_ids:
        return {}
    placeholders = ','.join(['?'] * len(order_ids))
    db = get_db()
    sql = f'''
        SELECT oi.id AS order_item_id,
               oi.order_id,
               oi.product_id,
               oi.quantity,
               oi.price,
               {PRODUCT_SELECT_COLUMNS}
        FROM order_item oi
        JOIN product p ON oi.product_id = p.id
        WHERE oi.order_id IN ({placeholders})
    '''
    cur = db.execute(sql, order_ids)
    items_map = {}
    for row in cur.fetchall():
        payload = build_order_item_payload(row)
        items_map.setdefault(row['order_id'], []).append(payload)
    return items_map


def attach_items_to_orders(order_rows):
    db_rows = [dict(r) for r in order_rows]
    order_ids = [row['id'] for row in db_rows]
    items_map = fetch_order_items_map(order_ids)
    for row in db_rows:
        row['items'] = items_map.get(row['id'], [])
    return db_rows


def load_favorite_target(db, target_type, target_id):
    if target_type not in ('SCENIC', 'PRODUCT'):
        return None
    table = 'scenic' if target_type == 'SCENIC' else 'product'
    cur = db.execute(f'SELECT * FROM {table} WHERE id = ?', (target_id,))
    return row_to_dict(cur.fetchone())


def build_favorite_payload(db, fav_row):
    if fav_row is None:
        return None
    return {
        'favorite_id': fav_row['id'],
        'user_id': fav_row['user_id'],
        'target_id': fav_row['target_id'],
        'target_type': fav_row['target_type'],
        'create_time': fav_row['create_time'],
        'target': load_favorite_target(db, fav_row['target_type'], fav_row['target_id']),
    }


def fetch_cart_item_payload(cart_id):
    db = get_db()
    sql = f'''
        SELECT c.id AS cart_id,
               c.user_id,
               c.quantity,
               c.create_time,
               {PRODUCT_SELECT_COLUMNS}
        FROM cart_item c
        JOIN product p ON c.product_id = p.id
        WHERE c.id = ?
    '''
    cur = db.execute(sql, (cart_id,))
    return build_cart_payload(cur.fetchone())


def fetch_visited_payload_by_id(visit_id):
    db = get_db()
    sql = f'''
        SELECT v.*,
               {SCENIC_SELECT_COLUMNS}
        FROM visited v
        JOIN scenic s ON v.scenic_id = s.id
        WHERE v.id = ?
    '''
    cur = db.execute(sql, (visit_id,))
    return build_visited_payload(cur.fetchone())


# -------------------- 健康检查 --------------------
# 用于确认服务是否启动。可在部署后用于负载均衡健康探测。
# /ping -> {"code":200, "msg":"OK", "data":{"msg":"pong"}}
@app.route('/ping')
def ping():
    return json_response(data={'msg': 'pong'})


# -------------------- 官网页面 --------------------
@app.route('/')
def home_page():
    db = get_db()
    tracked_tables = ['user', 'scenic', 'product', 'order_main', 'order_item',
                      'favorite', 'cart_item', 'visited', 'trip_plan']
    counts = {}
    for table_name in tracked_tables:
        try:
            cur = db.execute(f'SELECT COUNT(*) FROM {table_name}')
            counts[table_name] = cur.fetchone()[0] or 0
        except sqlite3.Error:
            counts[table_name] = 0

    installs = counts.get('user', 0)
    scenic_samples = counts.get('scenic', 0)
    product_samples = counts.get('product', 0)
    interaction_total = counts.get('order_item', 0) + counts.get('favorite', 0) + counts.get('cart_item', 0) \
        + counts.get('visited', 0) + counts.get('trip_plan', 0)
    api_calls = interaction_total + scenic_samples + product_samples
    feedback_rate = round((counts.get('visited', 0) / installs) * 100, 1) if installs else 0

    live_metrics = [
        {
            'label': '激活安装',
            'value': installs,
            'suffix': '+',
            'description': '注册 / 登录过的真实内测用户'
        },
        {
            'label': 'API 调用',
            'value': api_calls,
            'suffix': '',
            'description': '示例 API / Webhook 累计触发次数'
        },
        {
            'label': '反馈率',
            'value': feedback_rate,
            'suffix': '%',
            'description': 'Visited 数据量占用户总量的比例'
        },
        {
            'label': '行程计划',
            'value': counts.get('trip_plan', 0),
            'suffix': '',
            'description': 'Trip Plan / 行程模板已创建数量'
        }
    ]

    hero_modules = ['灵感流', '商城', '预订', '地图','个人中心']
    download_card = get_apk_metadata()
    data_counts = {
        'scenic': scenic_samples,
        'products': product_samples,
        'orders': counts.get('order_main', 0),
        'interactions': interaction_total
    }

    testimonials = [
        {
            'quote': '用 TravelMap 的 Demo 做路演，合作商一眼就明白产品节奏。',
            'author': '产品经理 · Leslie',
            'role': '泛旅行运营合作方'
        },
        {
            'quote': 'API + 示例数据库开箱即用，也方便课堂讲解电商链路。',
            'author': '厦门大学嘉庚学院',
            'role': '移动应用课程讲师'
        },
        {
            'quote': '底部四大模块串起来后，我们直接拿它做竞品对照。',
            'author': '自由设计师 Justyn',
            'role': '旅行产品设计顾问'
        }
    ]

    gallery_screens = [
        {'file': '全新底部导航栏-地图-具有全国所有景点景点位置信息.jpg', 'title': '地图页 · 全国坐标', 'tag': '地图',
         'description': '全新 Plan / Map 设计稿展示全国景点 Marker 与实时定位，是即将上线的重磅改版。'},
        {'file': '全新景点详情页-具有地图显示.jpg', 'title': '景点详情 + 地图', 'tag': '详情',
         'description': '景点详情在经纬度下方嵌入轻量地图，支持后续路线与导航扩展。'},
        {'file': '全新购物车界面-可以增加减少商品删除商品-下单需填写收货信息.jpg', 'title': '购物车流程升级', 'tag': '交易',
         'description': '新购物车 UI 支持数量增减、删除条目及完善收货信息，提交前校验更清晰。'},
        {'file': '全新订单详情页-就有更多信息展示.jpg', 'title': '订单详情页', 'tag': '订单',
         'description': '新增状态流转、联系人与子项列表，突出订单生命周期。'},
        {'file': '首页景点列表.jpg', 'title': '首页灵感流', 'tag': '发现',
         'description': 'Feed 卡片带地理信息、收藏、去过状态，一眼掌握库存情况。'},
        {'file': '景点详情页.jpg', 'title': '景点详情', 'tag': '详情',
         'description': '支持面包屑与浮层预订，顶部地图预留可扩展路线导航。'},
        {'file': '商城页面.jpg', 'title': '商城 Tab', 'tag': '交易',
         'description': '商品支持库存/售价/秒杀区分，底部 CTA 与购物车联动。'},
        {'file': '预定页面.jpg', 'title': '预订页', 'tag': '行程',
         'description': 'Booking 流程复刻 OTA 体验，订单详情可回查。'},
        {'file': '我的页.jpg', 'title': '个人中心', 'tag': '资产',
         'description': '聚合收藏、去过、订单、Coupon，暗色模式也有适配。'},
        {'file': '我的收藏页.jpg', 'title': '收藏列表', 'tag': '互动',
         'description': '收藏与去过与详情页实时同步，支持批量取消。'},
        {'file': '我的购物车页.jpg', 'title': '购物车', 'tag': '交易',
         'description': '购物车支持数量、勾选展示，将接入更多字段。'},
        {'file': '我的订单页面.jpg', 'title': '订单列表', 'tag': '订单',
         'description': '展示状态、金额、下单时间等字段，便于二开。'},
        {'file': '登录页.jpg', 'title': '登录页', 'tag': '账号',
         'description': '带表单校验与骨架屏动效，支持后续 OAuth 扩展。'},
        {'file': '注册页.jpg', 'title': '注册页', 'tag': '账号',
         'description': '注册流分离手机号/邮箱等信息，方便教学演示。'},
        {'file': '我的去过页.jpg', 'title': '去过记录', 'tag': '互动',
         'description': 'Visited 列表自动按时间排序，记录评分与时间。'},
        {'file': '深色模式适配.jpg', 'title': '暗色模式', 'tag': '外观',
         'description': '同一套组件库支持深浅色两种皮肤与动效。'}
    ]

    faq_entries = [
        {'question': '如何安装 APK？',
         'answer': '直接下载签名包，Android 9+ 允许「未知来源」安装即可，如需 CI 构建可 fork 仓库。'},
        {'question': '数据是否真实？',
         'answer': '示例数据库来自脱敏的景区/商品资料，可通过管理端导入自己的 CSV / API。'},
        {'question': '开源协议与商用限制？',
         'answer': '后端 MIT，前端 UI 也允许二次创作；使用真实业务数据时请遵守当地隐私法规。'},
        {'question': '如何安全使用 API？',
         'answer': '默认 SQLite + 简易登录，可在部署时改为 MySQL/PostgreSQL，并引入 JWT / HTTPS。'}
    ]

    cta_channels = [
        {'label': '加入内测群', 'href': f'{GITHUB_URL}/discussions', 'description': '同步版本动态、提交功能建议'},
        {'label': '订阅更新', 'href': '#subscribe', 'description': '邮箱订阅 DevLog，第一时间收到新版 APK'}
    ]

    base_url = request.url_root.rstrip('/')
    share_message = quote_plus('TravelMap Android 文旅助手，一站体验文旅业务链路')
    encoded_url = quote_plus(base_url)
    share_links = [
        {
            'label': '复制官网链接',
            'icon': '🔗',
            'action': 'copy'
        },
        {
            'label': '微博分享',
            'icon': '🌏',
            'href': f'https://service.weibo.com/share/share.php?url={encoded_url}&title={share_message}'
        },
        {
            'label': 'Twitter',
            'icon': '🐦',
            'href': f'https://twitter.com/intent/tweet?url={encoded_url}&text={share_message}'
        },
        {
            'label': 'Telegram',
            'icon': '✈️',
            'href': f'https://t.me/share/url?url={encoded_url}&text={share_message}'
        }
    ]

    return render_template(
        'home.html',
        github_url=GITHUB_URL,
        active='home',
        title='TravelMap · 智慧文旅后端',
        hero_modules=hero_modules,
        live_metrics=live_metrics,
        download_card=download_card,
        data_counts=data_counts,
        testimonials=testimonials,
        gallery_screens=gallery_screens,
        faq_entries=faq_entries,
        cta_channels=cta_channels,
        share_links=share_links,
        share_url=base_url
    )


@app.route('/docs')
def docs_page():
    return render_template('docs.html', github_url=GITHUB_URL, active='docs', title='TravelMap · 文档中心')


@app.route('/features')
def features_page():
    return render_template('features.html', github_url=GITHUB_URL, active='features', title='TravelMap · 功能总览')


@app.route('/api-explorer')
def api_explorer():
    base_api = request.url_root.rstrip('/')
    return render_template('api_docs.html', github_url=GITHUB_URL, active='api',
                           title='TravelMap · API Explorer', api_sections=API_SECTIONS,
                           base_api_url=base_api)


@app.route('/docs/file/<path:filename>')
def serve_doc_file(filename):
    doc_path = os.path.join(DOC_DIR, filename)
    if not os.path.isfile(doc_path):
        abort(404)
    download = request.args.get('download')
    return send_from_directory(DOC_DIR, filename, as_attachment=bool(download))


def load_markdown_file(filename):
    doc_path = os.path.join(DOC_DIR, filename)
    if not os.path.isfile(doc_path):
        abort(404)
    with open(doc_path, 'r', encoding='utf-8') as f:
        return f.read()


@app.route('/docs/view/<path:filename>')
def doc_view(filename):
    content = load_markdown_file(filename)
    return render_template('doc_view.html', github_url=GITHUB_URL, active='docs',
                           title=f'{filename} · 文档预览', markdown_text=content, filename=filename)


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
    user_row = db.execute('SELECT * FROM user WHERE id = ?', (user_id,)).fetchone()
    return json_response(200, '注册成功', {'user': sanitize_user_row(user_row)})


@app.route('/api/auth/login', methods=['POST'])
def login():
    data = get_json()
    username = data.get('username', '').strip()
    password = data.get('password', '').strip()

    if not username or not password:
        return json_response(400, '用户名和密码不能为空', None, 400)

    db = get_db()
    cur = db.execute('SELECT * FROM user WHERE username = ?', (username,))
    row = cur.fetchone()
    if row is None or not check_password_hash(row['password'], password):
        return json_response(401, '用户名或密码错误', None, 401)

    return json_response(200, '登录成功', {'user': sanitize_user_row(row)})


@app.route('/api/auth/wechat', methods=['POST'])
def wechat_login():
    """真实对接微信移动应用登录，使用 auth code 换取 openid 并维护用户。"""
    data = get_json()
    code = normalize_optional_str(data.get('code'))
    if not code:
        return json_response(400, 'code 不能为空', None, 400)

    try:
        token_payload = exchange_code_for_wechat_token(code)
    except WeChatConfigError as exc:
        return json_response(500, str(exc), None, 500)
    except WeChatAPIError as exc:
        return json_response(502, f'微信接口调用失败：{exc.errmsg}', {'errcode': exc.errcode}, 502)

    openid = token_payload['openid']
    unionid = token_payload.get('unionid')
    access_token = token_payload.get('access_token')
    refresh_token = token_payload.get('refresh_token')
    expires_at = compute_wechat_token_expire_at(token_payload.get('expires_in'))

    profile = fetch_wechat_user_profile(access_token, openid)
    nickname = normalize_optional_str(data.get('nickname')) or normalize_optional_str(profile.get('nickname'))
    avatar_url = normalize_optional_str(data.get('avatar_url')) or normalize_optional_str(profile.get('headimgurl'))
    if not nickname:
        nickname = f'微信用户_{openid[-4:]}'
    username = normalize_optional_str(data.get('username')) or nickname

    db = get_db()
    row = db.execute('SELECT * FROM user WHERE wx_openid = ?', (openid,)).fetchone()
    if row is None:
        cur = db.execute(
            'INSERT INTO user (login_type, username, nickname, avatar_url, wx_openid, wx_unionid, wx_access_token, wx_refresh_token, wx_token_expires_at) '
            'VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)',
            ('WECHAT', username, nickname, avatar_url, openid, unionid, access_token, refresh_token, expires_at)
        )
        db.commit()
        user_id = cur.lastrowid
    else:
        update_fields = []
        params = []

        def push(field, value):
            update_fields.append(f'{field} = ?')
            params.append(value)

        if access_token is not None:
            push('wx_access_token', access_token)
        if refresh_token is not None:
            push('wx_refresh_token', refresh_token)
        if expires_at is not None:
            push('wx_token_expires_at', expires_at)
        if unionid and not row['wx_unionid']:
            push('wx_unionid', unionid)

        should_update_avatar = not row['avatar_url'] or 'default_avatar' in str(row['avatar_url'])
        if avatar_url and should_update_avatar:
            push('avatar_url', avatar_url)

        should_update_nickname = not row['nickname'] or str(row['nickname']).startswith('微信用户_')
        if nickname and should_update_nickname:
            push('nickname', nickname)

        should_update_username = not row['username']
        if username and should_update_username:
            push('username', username)

        if update_fields:
            params.append(row['id'])
            db.execute(f"UPDATE user SET {', '.join(update_fields)} WHERE id = ?", params)
            db.commit()
        user_id = row['id']

    user_row = db.execute('SELECT * FROM user WHERE id = ?', (user_id,)).fetchone()
    sanitized = sanitize_user_row(user_row)
    app.logger.info(
        'WeChat login success user_id=%s openid=%s nickname=%s username=%s',
        user_id,
        openid,
        nickname,
        sanitized.get('username') if sanitized else None
    )
    return json_response(200, '微信登录成功', {'user': sanitized})


@app.route('/api/users/<int:user_id>', methods=['PUT'])
def update_user_contact(user_id):
    """前端 UserInfo 编辑页：仅允许修改手机号与邮箱。"""
    data = get_json()
    phone = normalize_optional_str(data.get('phone'))
    email = normalize_optional_str(data.get('email'))

    if phone is None and email is None:
        return json_response(400, '必须提供有效的 phone 或 email', None, 400)

    db = get_db()
    row = db.execute('SELECT * FROM user WHERE id = ?', (user_id,)).fetchone()
    if row is None:
        return json_response(404, '用户不存在', None, 404)

    fields = []
    params = []
    if phone is not None:
        fields.append('phone = ?')
        params.append(phone.strip() if isinstance(phone, str) else phone)
    if email is not None:
        fields.append('email = ?')
        params.append(email.strip() if isinstance(email, str) else email)

    params.append(user_id)
    sql = f'UPDATE user SET {", ".join(fields)} WHERE id = ?'
    db.execute(sql, params)
    db.commit()

    updated = db.execute('SELECT * FROM user WHERE id = ?', (user_id,)).fetchone()
    return json_response(200, '联系方式已更新', {'user': sanitize_user_row(updated)})


# =====================================================
# 二、景点模块 scenic（列表 / 搜索 / 详情 / 地图）
# =====================================================
# 支持 keyword 模糊匹配 name + description，city 精确匹配；默认返回全部匹配结果。
# 地图接口返回精简字段用于前端标点。


@app.route('/api/scenics', methods=['GET'])
def scenic_list():
    keyword = request.args.get('keyword', '').strip()
    city = request.args.get('city', '').strip()

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

    sql += ' ORDER BY id ASC'
    cur = db.execute(sql, params)
    rows = [dict(r) for r in cur.fetchall()]
    return json_response(200, 'OK', rows)


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
    cur = db.execute('SELECT * FROM scenic')
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

    sql += ' ORDER BY id ASC'
    cur = db.execute(sql, params)
    rows = [dict(r) for r in cur.fetchall()]

    return json_response(200, 'OK', rows)


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

    if not btype:
        return json_response(400, 'type 参数必填(HOTEL/TICKET)', None, 400)

    db = get_db()
    base_sql = 'FROM product p LEFT JOIN scenic s ON p.scenic_id = s.id WHERE p.type = ?'
    params = [btype]
    if city:
        base_sql += ' AND s.city = ?'
        params.append(city)

    data_sql = f'''
        SELECT {PRODUCT_SELECT_COLUMNS},
               {SCENIC_SELECT_COLUMNS}
        {base_sql}
        ORDER BY p.id ASC
    '''
    cur = db.execute(data_sql, params)
    results = []
    for row in cur.fetchall():
        product = extract_prefixed_fields(row, 'product_')
        scenic = extract_prefixed_fields(row, 'scenic_')
        results.append({'product': product, 'scenic': scenic or None})

    return json_response(200, 'OK', results)


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
    cur = db.execute('SELECT * FROM favorite WHERE user_id = ? AND target_id = ? AND target_type = ?',
                     (user_id, target_id, target_type))
    existing = cur.fetchone()
    if existing:
        return json_response(200, '已收藏', {'favorite': build_favorite_payload(db, existing)})

    cur = db.execute('INSERT INTO favorite (user_id, target_id, target_type, create_time) VALUES (?, ?, ?, ?)',
                     (user_id, target_id, target_type, datetime.now().strftime('%Y-%m-%d %H:%M:%S')))
    db.commit()
    fav_row = db.execute('SELECT * FROM favorite WHERE id = ?', (cur.lastrowid,)).fetchone()
    return json_response(200, '收藏成功', {'favorite': build_favorite_payload(db, fav_row)})


@app.route('/api/favorites', methods=['DELETE'])
def remove_favorite():
    data = get_json()
    user_id = data.get('user_id')
    target_id = data.get('target_id')
    target_type = (data.get('target_type') or '').upper()

    if not all([user_id, target_id, target_type]):
        return json_response(400, 'user_id/target_id/target_type 必填', None, 400)

    db = get_db()
    cur = db.execute('SELECT * FROM favorite WHERE user_id = ? AND target_id = ? AND target_type = ?',
                     (user_id, target_id, target_type))
    row = cur.fetchone()
    if row is None:
        return json_response(200, '收藏记录不存在，视为已取消', {'favorite': None, 'deleted': False})
    db.execute('DELETE FROM favorite WHERE id = ?', (row['id'],))
    db.commit()
    return json_response(200, '已取消收藏', {'favorite': build_favorite_payload(db, row), 'deleted': True})


@app.route('/api/favorites/status', methods=['GET'])
def favorite_status():
    user_id = request.args.get('user_id')
    target_id = request.args.get('target_id')
    target_type = (request.args.get('target_type') or '').upper()

    if not all([user_id, target_id, target_type]):
        return json_response(400, 'user_id/target_id/target_type 必填', None, 400)

    db = get_db()
    cur = db.execute('SELECT * FROM favorite WHERE user_id = ? AND target_id = ? AND target_type = ?',
                     (user_id, target_id, target_type))
    row = cur.fetchone()
    payload = build_favorite_payload(db, row)
    return json_response(200, 'OK', {'favorited': payload is not None, 'favorite': payload})


@app.route('/api/favorites/scenics', methods=['GET'])
def my_fav_scenics():
    user_id = request.args.get('user_id')
    if not user_id:
        return json_response(400, 'user_id 必填', None, 400)

    db = get_db()
    cur = db.execute('SELECT * FROM favorite WHERE user_id = ? AND target_type = "SCENIC" ORDER BY create_time DESC',
                     (user_id,))
    rows = [build_favorite_payload(db, row) for row in cur.fetchall()]
    return json_response(200, 'OK', rows)


@app.route('/api/favorites/products', methods=['GET'])
def my_fav_products():
    user_id = request.args.get('user_id')
    if not user_id:
        return json_response(400, 'user_id 必填', None, 400)

    db = get_db()
    cur = db.execute('SELECT * FROM favorite WHERE user_id = ? AND target_type = "PRODUCT" ORDER BY create_time DESC',
                     (user_id,))
    rows = [build_favorite_payload(db, row) for row in cur.fetchall()]
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
    cur = db.execute('INSERT INTO visited (user_id, scenic_id, visit_date, rating) VALUES (?, ?, ?, ?)',
                     (user_id, scenic_id, datetime.now().strftime('%Y-%m-%d'), rating))
    db.commit()
    payload = fetch_visited_payload_by_id(cur.lastrowid)
    return json_response(200, '已标记为去过', {'visited': payload})


@app.route('/api/visited', methods=['GET'])
def list_visited():
    user_id = request.args.get('user_id')
    if not user_id:
        return json_response(400, 'user_id 必填', None, 400)

    db = get_db()
    sql = f'''
        SELECT v.*,
               {SCENIC_SELECT_COLUMNS}
        FROM visited v
        JOIN scenic s ON v.scenic_id = s.id
        WHERE v.user_id = ?
        ORDER BY v.visit_date DESC
    '''
    cur = db.execute(sql, (user_id,))
    rows = [build_visited_payload(row) for row in cur.fetchall()]
    return json_response(200, 'OK', rows)


@app.route('/api/visited/<int:visit_id>', methods=['DELETE'])
def delete_visited(visit_id):
    user_id = request.args.get('user_id')
    if not user_id:
        data = get_json()
        user_id = data.get('user_id')
    if not user_id:
        return json_response(400, 'user_id 必填', None, 400)

    db = get_db()
    row = db.execute('SELECT user_id FROM visited WHERE id = ?', (visit_id,)).fetchone()
    if row is None:
        return json_response(404, '去过记录不存在', None, 404)
    if str(row['user_id']) != str(user_id):
        return json_response(403, '无权删除该记录', None, 403)

    payload = fetch_visited_payload_by_id(visit_id)
    db.execute('DELETE FROM visited WHERE id = ?', (visit_id,))
    db.commit()
    return json_response(200, '去过记录已删除', {'visited': payload, 'deleted': True})


@app.route('/api/visited', methods=['DELETE'])
def delete_visited_by_scenic():
    user_id = request.args.get('user_id')
    scenic_id = request.args.get('scenic_id')
    if not user_id or not scenic_id:
        data = get_json()
        user_id = user_id or data.get('user_id')
        scenic_id = scenic_id or data.get('scenic_id')
    if not all([user_id, scenic_id]):
        return json_response(400, 'user_id/scenic_id 必填', None, 400)

    db = get_db()
    cur = db.execute('SELECT id FROM visited WHERE user_id = ? AND scenic_id = ?', (user_id, scenic_id))
    row = cur.fetchone()
    if row is None:
        return json_response(200, '记录不存在，视为已取消', {'visited': None, 'deleted': False})

    payload = fetch_visited_payload_by_id(row['id'])
    db.execute('DELETE FROM visited WHERE id = ?', (row['id'],))
    db.commit()
    return json_response(200, '去过记录已删除', {'visited': payload, 'deleted': True})


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
        cart_id = row['id']
    else:
        cur = db.execute('INSERT INTO cart_item (user_id, product_id, quantity, create_time) VALUES (?, ?, ?, ?)',
                         (user_id, product_id, quantity, datetime.now().strftime('%Y-%m-%d %H:%M:%S')))
        cart_id = cur.lastrowid
    db.commit()
    payload = fetch_cart_item_payload(cart_id)
    return json_response(200, '加入购物车成功', {'cart_item': payload})


@app.route('/api/cart/<int:cart_id>', methods=['PUT'])
def update_cart_item(cart_id):
    data = get_json()
    raw_q = data.get('quantity')
    quantity = 1 if raw_q is None else int(raw_q)
    if quantity <= 0:
        return json_response(400, 'quantity 必须大于 0', None, 400)
    db = get_db()
    cur = db.execute('SELECT id FROM cart_item WHERE id = ?', (cart_id,))
    if cur.fetchone() is None:
        return json_response(404, '购物车条目不存在', None, 404)
    db.execute('UPDATE cart_item SET quantity = ? WHERE id = ?', (quantity, cart_id))
    db.commit()
    payload = fetch_cart_item_payload(cart_id)
    return json_response(200, '修改成功', {'cart_item': payload})


@app.route('/api/cart/<int:cart_id>', methods=['DELETE'])
def delete_cart_item(cart_id):
    payload = fetch_cart_item_payload(cart_id)
    if payload is None:
        return json_response(404, '购物车条目不存在', None, 404)
    db = get_db()
    db.execute('DELETE FROM cart_item WHERE id = ?', (cart_id,))
    db.commit()
    return json_response(200, '删除成功', {'cart_item': payload, 'deleted': True})


@app.route('/api/cart', methods=['GET'])
def list_cart():
    user_id = request.args.get('user_id')
    if not user_id:
        return json_response(400, 'user_id 必填', None, 400)

    db = get_db()
    sql = f'''
        SELECT c.id AS cart_id,
               c.user_id,
               c.quantity,
               c.create_time,
               {PRODUCT_SELECT_COLUMNS}
        FROM cart_item c
        JOIN product p ON c.product_id = p.id
        WHERE c.user_id = ?
        ORDER BY c.create_time DESC, c.id DESC
    '''
    cur = db.execute(sql, (user_id,))
    items = [build_cart_payload(row) for row in cur.fetchall()]
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
    sql = f'''
        SELECT c.quantity,
               {PRODUCT_SELECT_COLUMNS}
        FROM cart_item c
        JOIN product p ON c.product_id = p.id
        WHERE c.user_id = ?
    '''
    cur = db.execute(sql, (user_id,))
    items = cur.fetchall()
    if not items:
        return json_response(400, '购物车为空', None, 400)

    total_price = sum(row['product_price'] * row['quantity'] for row in items)
    order_no = datetime.now().strftime('%Y%m%d%H%M%S') + uuid.uuid4().hex[:6]

    cur = db.execute(
        'INSERT INTO order_main (order_no, user_id, order_type, total_price, status, create_time, contact_name, contact_phone, checkin_date, checkout_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
        (order_no, user_id, order_type, total_price, 'CREATED', datetime.now().strftime('%Y-%m-%d %H:%M:%S'),
         contact_name, contact_phone, checkin_date, checkout_date))
    order_id = cur.lastrowid

    for row in items:
        db.execute('INSERT INTO order_item (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)',
                   (order_id, row['product_id'], row['quantity'], row['product_price']))

    db.execute('DELETE FROM cart_item WHERE user_id = ?', (user_id,))
    db.commit()

    order_row = db.execute('SELECT * FROM order_main WHERE id = ?', (order_id,)).fetchone()
    order_payload = attach_items_to_orders([order_row])[0]
    return json_response(200, '下单成功', {'order': order_payload})


@app.route('/api/orders', methods=['GET'])
def list_orders():
    user_id = request.args.get('user_id')
    if not user_id:
        return json_response(400, 'user_id 必填', None, 400)

    db = get_db()
    sql = 'SELECT * FROM order_main WHERE user_id = ? ORDER BY create_time DESC'
    cur = db.execute(sql, (user_id,))
    rows = attach_items_to_orders(cur.fetchall())
    return json_response(200, 'OK', rows)


@app.route('/api/orders/<int:oid>', methods=['GET'])
def order_detail(oid):
    db = get_db()
    cur = db.execute('SELECT * FROM order_main WHERE id = ?', (oid,))
    order = cur.fetchone()
    if order is None:
        return json_response(404, '订单不存在', None, 404)

    payload = attach_items_to_orders([order])[0]
    return json_response(200, 'OK', {'order': payload})


# =====================================================
# 主入口 main
# =====================================================
# debug=True 便于开发查看错误堆栈；生产环境建议关闭并使用 gunicorn / waitress 等 WSGI 部署。
# host=0.0.0.0 允许局域网设备（手机 / 模拟器）访问。
if __name__ == '__main__':
    # 端口改为 5001 避免本机 5000 已被占用
    app.run(host='0.0.0.0', port=5001, debug=True)
