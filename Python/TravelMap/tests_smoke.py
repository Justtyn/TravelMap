# -*- coding: utf-8 -*-
"""TravelMap 全量冒烟与压力测试脚本
运行方式:
    python tests_smoke.py
说明: 使用 Flask app.test_client()，涵盖全部接口、常见异常与重复访问压力测试。
"""

import json
import random
import sqlite3
import uuid

from werkzeug.security import generate_password_hash

from app import app, DB_PATH

random.seed(42)

SCENIC_TEMPLATES = [
    ('鼓浪屿', '厦门', 24.449, 118.073),
    ('西湖', '杭州', 30.241, 120.150),
    ('张家界', '张家界', 29.117, 110.479),
    ('稻城亚丁', '甘孜', 28.589, 100.292),
    ('滕王阁', '南昌', 28.684, 115.893),
    ('乌镇', '嘉兴', 30.748, 120.494),
]


# 统一打印辅助
def p(label, resp):
    data = resp.get_json()
    print(f'[{label}] status={resp.status_code} code={data.get("code")} msg={data.get("msg")}')
    return data


def ensure_seed():
    """基于 schema 构造随机假数据，保证各业务入口有可用样本。"""
    conn = sqlite3.connect(DB_PATH)
    conn.execute('PRAGMA foreign_keys = ON;')
    cur = conn.cursor()
    ensure_base_user(cur)
    seed_scenics(cur)
    seed_products(cur)
    conn.commit()
    conn.close()


def ensure_base_user(cur):
    row = cur.execute('SELECT id FROM user WHERE username = ?', ('user1',)).fetchone()
    if row:
        return row[0]
    pwd_hash = generate_password_hash('pwd123')
    cur.execute(
        'INSERT INTO user (login_type, username, password, phone, email, nickname, avatar_url, wx_unionid, wx_openid, wx_access_token, wx_refresh_token, wx_token_expires_at) '
        'VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)',
        ('LOCAL', 'user1', pwd_hash, '13800000000', 'user1@example.com', '演示账号', 'https://img.example.com/avatar/user1.png',
         None, None, None, None, None)
    )
    return cur.lastrowid


def seed_scenics(cur):
    count = cur.execute('SELECT COUNT(*) FROM scenic').fetchone()[0]
    if count:
        return
    for idx, (name, city, latitude, longitude) in enumerate(SCENIC_TEMPLATES):
        suffix = random.randint(10, 99)
        cur.execute(
            'INSERT INTO scenic (name, city, cover_image, description, address, latitude, longitude, audio_url) '
            'VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
            (f'{name}{suffix}', city, f'{city.lower()}_{idx}.jpg', f'{name} 随机景点介绍 {suffix}',
             f'{city}市热门路{suffix}号', latitude + random.uniform(-0.02, 0.02),
             longitude + random.uniform(-0.02, 0.02), f'https://audio.example.com/{city.lower()}_{idx}.mp3')
        )


def seed_products(cur):
    count = cur.execute('SELECT COUNT(*) FROM product').fetchone()[0]
    if count:
        return
    scenic_rows = cur.execute('SELECT id, name, city FROM scenic').fetchall()
    if not scenic_rows:
        return
    for scenic_id, scenic_name, city in scenic_rows[:4]:
        hotel_price = round(random.uniform(420, 980), 2)
        cur.execute(
            'INSERT INTO product (name, scenic_id, cover_image, price, stock, description, type, hotel_address) '
            'VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
            (f'{scenic_name} 轻奢酒店', scenic_id, f'{scenic_name}_hotel.jpg', hotel_price, random.randint(5, 25),
             f'{scenic_name} 高品质住宿体验', 'HOTEL', f'{city}市中心大道{random.randint(1, 200)}号')
        )
        cur.execute(
            'INSERT INTO product (name, scenic_id, cover_image, price, stock, description, type, hotel_address) '
            'VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
            (f'{scenic_name} 景区通票', scenic_id, f'{scenic_name}_ticket.jpg', round(random.uniform(50, 220), 2),
             random.randint(100, 1000), f'{scenic_name} 门票+讲解', 'TICKET', None)
        )
    for idx in range(3):
        cur.execute(
            'INSERT INTO product (name, scenic_id, cover_image, price, stock, description, type, hotel_address) '
            'VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
            (f'环球旅行盲盒{idx}', None, f'blindbox_{idx}.jpg', round(random.uniform(20, 150), 2),
             random.randint(100, 800), '通用旅行周边', 'TRAVEL', None)
        )


def first_or_none(data, path):
    """从 data dict 中安全取值，path 为键列表。"""
    node = data
    for key in path:
        if not isinstance(node, dict):
            return None
        node = node.get(key)
        if node is None:
            return None
    return node


def main():
    ensure_seed()
    client = app.test_client()

    # 1. 健康检查
    r = client.get('/ping')
    p('PING', r)

    # 2. 注册 - 正常/异常
    uname = 'user1_' + uuid.uuid4().hex[:6]
    reg_payload = {'username': uname, 'password': 'pwd123', 'nickname': '测试用户', 'phone': '18800001111'}
    reg_data = p('REGISTER_OK', client.post('/api/auth/register', json=reg_payload))
    reg_user = first_or_none(reg_data, ['data', 'user']) or {}
    user_id = reg_user.get('id')
    if not user_id:
        print('注册未返回 user.id，退出')
        return
    p('REGISTER_DUP', client.post('/api/auth/register', json={'username': uname, 'password': 'pwd123'}))
    p('REGISTER_BAD', client.post('/api/auth/register', json={'username': '', 'password': ''}))

    # 3. 登录与微信登录
    login_ok = p('LOGIN_OK', client.post('/api/auth/login', json={'username': 'user1', 'password': 'pwd123'}))
    assert first_or_none(login_ok, ['data', 'user', 'username']) == 'user1'
    p('LOGIN_BAD_PWD', client.post('/api/auth/login', json={'username': 'user1', 'password': 'x'}))
    p('LOGIN_BAD_PARAM', client.post('/api/auth/login', json={'username': '', 'password': ''}))
    p('WECHAT_NO_CODE', client.post('/api/auth/wechat', json={}))
    wechat = p('WECHAT_OK', client.post('/api/auth/wechat', json={'code': 'abc1234'}))
    assert first_or_none(wechat, ['data', 'user', 'nickname'])

    # 4. 景点接口 + 分页压力
    scenic_resp = p('SCENIC_LIST', client.get('/api/scenics'))
    scenic_items = scenic_resp.get('data', {}).get('list') or []
    scenic_id = scenic_items[0]['id'] if scenic_items else None
    scenic_city = scenic_items[0].get('city') if scenic_items else '厦门'
    for size in (2, 3):
        scenic_page = p(f'SCENIC_PAGE_SIZE_{size}', client.get(f'/api/scenics?page=1&page_size={size}'))
        assert scenic_page['data']['page_size'] == size
    p('SCENIC_LIST_KW', client.get('/api/scenics?keyword=景'))
    p('SCENIC_LIST_CITY', client.get('/api/scenics?city=杭州'))
    if scenic_id:
        p('SCENIC_DETAIL_OK', client.get(f'/api/scenics/{scenic_id}'))
    invalid_scenic = (scenic_id or 0) + 9999
    p('SCENIC_DETAIL_404', client.get(f'/api/scenics/{invalid_scenic}'))
    scenic_map = p('SCENIC_MAP', client.get('/api/scenics/map'))
    if scenic_map.get('data'):
        assert 'audio_url' in scenic_map['data'][0]

    # 5. 商品与预订
    product_resp = p('PRODUCT_LIST', client.get('/api/products'))
    product_items = product_resp.get('data', {}).get('list') or []
    product_id_primary = product_items[0]['id'] if product_items else None
    product_id_secondary = product_items[1]['id'] if len(product_items) > 1 else product_id_primary
    p('PRODUCT_LIST_TYPE_HOTEL', client.get('/api/products?type=HOTEL'))
    p('PRODUCT_LIST_KW', client.get('/api/products?keyword=通票'))
    p('PRODUCT_PAGE_STRESS', client.get('/api/products?page=2&page_size=2'))
    if product_id_primary:
        p('PRODUCT_DETAIL_OK', client.get(f'/api/products/{product_id_primary}'))
    p('PRODUCT_DETAIL_404', client.get('/api/products/9999'))
    p('BOOKING_NO_TYPE', client.get('/api/bookings'))
    hotel_city = scenic_city or '厦门'
    booking_hotel = p('BOOKING_HOTEL_CITY', client.get(f'/api/bookings?type=HOTEL&city={hotel_city}'))
    if booking_hotel.get('data', {}).get('list'):
        assert booking_hotel['data']['list'][0]['product']
    booking_ticket = p('BOOKING_TICKET', client.get('/api/bookings?type=TICKET'))
    assert booking_ticket['data']['total'] >= 1

    # 6. 行程计划
    plan_payload = {
        'user_id': user_id,
        'title': '江南三日游',
        'start_date': '2025-05-01',
        'end_date': '2025-05-03',
        'source': 'MANUAL',
        'content': json.dumps({'days': 3, 'notes': ['试运行']})
    }
    plan_data = p('PLAN_CREATE', client.post('/api/plans', json=plan_payload))
    plan_id = first_or_none(plan_data, ['data', 'plan_id'])
    p('PLAN_LIST', client.get(f'/api/plans?user_id={user_id}'))
    if plan_id:
        p('PLAN_DETAIL_OK', client.get(f'/api/plans/{plan_id}'))
    p('PLAN_DETAIL_404', client.get('/api/plans/9999'))

    # 7. 收藏 scenic/product
    if scenic_id:
        fav_scenic = p('FAV_SCENIC_ADD', client.post('/api/favorites', json={'user_id': user_id, 'target_id': scenic_id, 'target_type': 'SCENIC'}))
        assert first_or_none(fav_scenic, ['data', 'favorite', 'target'])
        p('FAV_SCENIC_DUP', client.post('/api/favorites', json={'user_id': user_id, 'target_id': scenic_id, 'target_type': 'SCENIC'}))
        p('FAV_SCENIC_LIST', client.get(f'/api/favorites/scenics?user_id={user_id}'))
        p('FAV_SCENIC_DEL', client.delete('/api/favorites', json={'user_id': user_id, 'target_id': scenic_id, 'target_type': 'SCENIC'}))
    else:
        print('无景点数据，跳过景点收藏测试')

    if product_id_primary:
        fav_product = p('FAV_PRODUCT_ADD', client.post('/api/favorites', json={'user_id': user_id, 'target_id': product_id_primary, 'target_type': 'PRODUCT'}))
        assert first_or_none(fav_product, ['data', 'favorite', 'target', 'price']) is not None
        p('FAV_PRODUCT_LIST', client.get(f'/api/favorites/products?user_id={user_id}'))
    else:
        print('无商品数据，跳过商品收藏测试')

    # 8. 去过 visited
    if scenic_id:
        visited = p('VISITED_ADD', client.post('/api/visited', json={'user_id': user_id, 'scenic_id': scenic_id, 'rating': 5}))
        assert first_or_none(visited, ['data', 'visited', 'scenic'])
        p('VISITED_ADD_NO_RATING', client.post('/api/visited', json={'user_id': user_id, 'scenic_id': scenic_id}))
        p('VISITED_LIST', client.get(f'/api/visited?user_id={user_id}'))
    else:
        print('无景点数据，跳过去过记录测试')
    p('VISITED_NO_USER', client.get('/api/visited'))

    # 9 & 10. 购物车 + 订单流
    if product_id_primary:
        cart1 = p('CART_ADD1', client.post('/api/cart', json={'user_id': user_id, 'product_id': product_id_primary, 'quantity': 2}))
        assert first_or_none(cart1, ['data', 'cart_item', 'product', 'price'])
        p('CART_ADD_AGAIN', client.post('/api/cart', json={'user_id': user_id, 'product_id': product_id_primary, 'quantity': 1}))
        if product_id_secondary:
            p('CART_ADD2', client.post('/api/cart', json={'user_id': user_id, 'product_id': product_id_secondary, 'quantity': 1}))
        cart_list = p('CART_LIST', client.get(f'/api/cart?user_id={user_id}'))
        cart_items = cart_list.get('data') or []
        first_cart_id = cart_items[0]['cart_id'] if cart_items else None
        if first_cart_id:
            p('CART_UPDATE_OK', client.put(f'/api/cart/{first_cart_id}', json={'quantity': 5}))
            p('CART_UPDATE_BAD', client.put(f'/api/cart/{first_cart_id}', json={'quantity': 0}))
            p('CART_DELETE', client.delete(f'/api/cart/{first_cart_id}'))
            p('CART_UPDATE_NOT_FOUND', client.put('/api/cart/999999', json={'quantity': 1}))
            p('CART_DELETE_NOT_FOUND', client.delete('/api/cart/999999'))
        p('CART_LIST_AFTER_DEL', client.get(f'/api/cart?user_id={user_id}'))

        # 订单创建/查询/详情/异常
        target_order_pid = product_id_secondary or product_id_primary
        p('CART_ADD_FOR_ORDER', client.post('/api/cart', json={'user_id': user_id, 'product_id': target_order_pid, 'quantity': 3}))
        order_data = p('ORDER_CREATE', client.post('/api/orders', json={
            'user_id': user_id,
            'contact_name': '张三',
            'contact_phone': '13800138000',
            'order_type': 'HOTEL',
            'checkin_date': '2025-11-20',
            'checkout_date': '2025-11-21'
        }))
        order_payload = first_or_none(order_data, ['data', 'order']) or {}
        order_id = order_payload.get('id')
        assert order_payload.get('items'), '订单缺少明细'
        order_list = p('ORDER_LIST', client.get(f'/api/orders?user_id={user_id}'))
        order_entries = order_list.get('data') or []
        if order_entries:
            assert order_entries[0]['items']
        if order_id:
            p('ORDER_DETAIL_OK', client.get(f'/api/orders/{order_id}'))
        p('ORDER_DETAIL_404', client.get('/api/orders/99999'))
        p('ORDER_CART_EMPTY', client.post('/api/orders', json={'user_id': user_id}))
        p('ORDER_NO_USER', client.post('/api/orders', json={}))
    else:
        print('无商品数据，跳过购物车与订单测试')

    # 11. 预订/商品接口压力循环
    for idx in range(3):
        p(f'STRESS_BOOKING_HOTEL_{idx}', client.get('/api/bookings?type=HOTEL'))
        p(f'STRESS_PRODUCTS_{idx}', client.get(f'/api/products?page=1&page_size={idx + 1}'))

    print('\n== 冒烟 + 压力测试完成 ==')


if __name__ == '__main__':
    main()
