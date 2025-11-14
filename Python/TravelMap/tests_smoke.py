# -*- coding: utf-8 -*-
"""TravelMap 全量冒烟测试脚本
运行方式:
    python tests_smoke.py
说明: 使用 Flask 提供的 app.test_client()，不依赖外部服务器监听端口。
"""
import json
import sqlite3
import os
import uuid
from app import app, DB_PATH


# 统一打印辅助
def p(label, resp):
    data = resp.get_json()
    print(f'[{label}] status={resp.status_code} code={data.get("code")} msg={data.get("msg")}')
    return data


def ensure_seed():
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()
    # 如果没有景点与商品则插入少量种子数据
    scenic_count = cur.execute('SELECT COUNT(*) FROM scenic').fetchone()[0]
    if scenic_count == 0:
        cur.execute(
            "INSERT INTO scenic (name, city, cover_image, description, address, latitude, longitude) VALUES (?,?,?,?,?,?,?)",
            (
                '天安门', '北京', 'tiananmen.jpg', '天安门广场', '北京市东城区', 39.9087, 116.3975
            ))
        cur.execute(
            "INSERT INTO scenic (name, city, cover_image, description, address, latitude, longitude) VALUES (?,?,?,?,?,?,?)",
            (
                '故宫', '北京', 'gugong.jpg', '故宫博物院', '北京市东城区景山前街4号', 39.9163, 116.3972
            ))
    product_count = cur.execute('SELECT COUNT(*) FROM product').fetchone()[0]
    if product_count == 0:
        # HOTEL 商品关联 scenic 1
        cur.execute(
            "INSERT INTO product (name, scenic_id, cover_image, price, stock, description, type, hotel_address) VALUES (?,?,?,?,?,?,?,?)",
            (
                '北京饭店大床房', 1, 'hotel1.jpg', 688.0, 10, '舒适大床房', 'HOTEL', '北京市东城区长安街33号'
            ))
        # TICKET 商品关联 scenic 2
        cur.execute(
            "INSERT INTO product (name, scenic_id, cover_image, price, stock, description, type, hotel_address) VALUES (?,?,?,?,?,?,?,?)",
            (
                '故宫门票', 2, 'ticket1.jpg', 60.0, 1000, '参观故宫博物院门票', 'TICKET', None
            ))
        # TRAVEL 周边不关联 scenic
        cur.execute(
            "INSERT INTO product (name, scenic_id, cover_image, price, stock, description, type, hotel_address) VALUES (?,?,?,?,?,?,?,?)",
            (
                '北京冰箱贴', None, 'souvenir1.jpg', 25.0, 500, '特色冰箱贴', 'TRAVEL', None
            ))
    conn.commit()
    conn.close()


def main():
    ensure_seed()
    client = app.test_client()

    # 1. 健康检查
    r = client.get('/ping');
    p('PING', r)

    # 2. 注册 - 正常
    uname = 'user1_' + uuid.uuid4().hex[:6]
    r = client.post('/api/auth/register', json={'username': uname, 'password': 'pwd123', 'nickname': '测试用户'})
    reg_data = p('REGISTER_OK', r)
    user_id = (reg_data.get('data') or {}).get('user_id')
    if not user_id:
        print('注册未获取 user_id，退出测试')
        return
    # 2.1 注册 - 重复
    r = client.post('/api/auth/register', json={'username': uname, 'password': 'pwd123'})
    p('REGISTER_DUP', r)
    # 2.2 注册 - 缺失字段
    r = client.post('/api/auth/register', json={'username': '', 'password': ''})
    p('REGISTER_BAD', r)

    # 3. 登录 - 成功
    r = client.post('/api/auth/login', json={'username': 'user1', 'password': 'pwd123'})
    login_ok = p('LOGIN_OK', r)
    # 3.1 登录 - 密码错误
    r = client.post('/api/auth/login', json={'username': 'user1', 'password': 'x'})
    p('LOGIN_BAD_PWD', r)
    # 3.2 登录 - 缺字段
    r = client.post('/api/auth/login', json={'username': '', 'password': ''})
    p('LOGIN_BAD_PARAM', r)

    # 4. 微信登录 - 缺 code
    r = client.post('/api/auth/wechat', json={})
    p('WECHAT_NO_CODE', r)
    # 4.1 微信登录 - 正常
    r = client.post('/api/auth/wechat', json={'code': 'abc1234'})
    p('WECHAT_OK', r)

    # 5. 景点列表 空筛选 与 keyword / city
    r = client.get('/api/scenics')
    p('SCENIC_LIST', r)
    r = client.get('/api/scenics?keyword=天安门')
    p('SCENIC_LIST_KW', r)
    r = client.get('/api/scenics?city=北京')
    p('SCENIC_LIST_CITY', r)
    # 5.1 景点详情 OK & 404
    r = client.get('/api/scenics/1');
    p('SCENIC_DETAIL_OK', r)
    r = client.get('/api/scenics/999');
    p('SCENIC_DETAIL_404', r)
    # 5.2 地图
    r = client.get('/api/scenics/map');
    p('SCENIC_MAP', r)

    # 6. 商品列表 泛查询 / type / keyword
    r = client.get('/api/products');
    p('PRODUCT_LIST', r)
    r = client.get('/api/products?type=HOTEL');
    p('PRODUCT_LIST_TYPE_HOTEL', r)
    r = client.get('/api/products?keyword=门票');
    p('PRODUCT_LIST_KW', r)
    # 6.1 商品详情 OK & 404
    r = client.get('/api/products/1');
    p('PRODUCT_DETAIL_OK', r)
    r = client.get('/api/products/999');
    p('PRODUCT_DETAIL_404', r)

    # 7. 预订列表 缺 type / type+city / type only
    r = client.get('/api/bookings');
    p('BOOKING_NO_TYPE', r)
    r = client.get('/api/bookings?type=HOTEL&city=北京');
    p('BOOKING_HOTEL_CITY', r)
    r = client.get('/api/bookings?type=TICKET');
    p('BOOKING_TICKET', r)

    # 8. 行程计划 创建 / 列表 / 详情 / 404
    r = client.post('/api/plans', json={'user_id': user_id, 'title': '北京2日游', 'start_date': '2025-11-20',
                                        'end_date': '2025-11-21', 'content': json.dumps({'days': 2})})
    plan_data = p('PLAN_CREATE', r)
    plan_id = plan_data.get('data', {}).get('plan_id')
    r = client.get(f'/api/plans?user_id={user_id}');
    p('PLAN_LIST', r)
    r = client.get(f'/api/plans/{plan_id}');
    p('PLAN_DETAIL_OK', r)
    r = client.get('/api/plans/9999');
    p('PLAN_DETAIL_404', r)

    # 9. 收藏 scenic / product 添加 / 重复 / 列表 / 删除
    r = client.post('/api/favorites', json={'user_id': user_id, 'target_id': 1, 'target_type': 'SCENIC'});
    p('FAV_SCENIC_ADD', r)
    r = client.post('/api/favorites', json={'user_id': user_id, 'target_id': 1, 'target_type': 'SCENIC'});
    p('FAV_SCENIC_DUP', r)
    r = client.get(f'/api/favorites/scenics?user_id={user_id}');
    p('FAV_SCENIC_LIST', r)
    r = client.delete('/api/favorites', json={'user_id': user_id, 'target_id': 1, 'target_type': 'SCENIC'});
    p('FAV_SCENIC_DEL', r)
    r = client.get(f'/api/favorites/scenics?user_id={user_id}');
    p('FAV_SCENIC_LIST_AFTER_DEL', r)
    # product 收藏
    r = client.post('/api/favorites', json={'user_id': user_id, 'target_id': 2, 'target_type': 'PRODUCT'});
    p('FAV_PRODUCT_ADD', r)
    r = client.get(f'/api/favorites/products?user_id={user_id}');
    p('FAV_PRODUCT_LIST', r)

    # 10. 去过 visited 添加 / 列表 缺 user_id
    r = client.post('/api/visited', json={'user_id': user_id, 'scenic_id': 1, 'rating': 5});
    p('VISITED_ADD', r)
    r = client.get(f'/api/visited?user_id={user_id}');
    p('VISITED_LIST', r)
    r = client.get('/api/visited');
    p('VISITED_NO_USER', r)

    # 11. 购物车 加入 / 修改 / 列表 / 删除
    r = client.post('/api/cart', json={'user_id': user_id, 'product_id': 1, 'quantity': 2});
    p('CART_ADD1', r)
    r = client.post('/api/cart', json={'user_id': user_id, 'product_id': 1, 'quantity': 1});
    p('CART_ADD_AGAIN', r)
    r = client.post('/api/cart', json={'user_id': user_id, 'product_id': 2, 'quantity': 1});
    p('CART_ADD2', r)
    r = client.get(f'/api/cart?user_id={user_id}');
    cart_list = p('CART_LIST', r)
    # 修改第一条数量
    first_cart_id = cart_list.get('data', [])[0]['cart_id'] if cart_list.get('data') else None
    if first_cart_id:
        r = client.put(f'/api/cart/{first_cart_id}', json={'quantity': 5});
        p('CART_UPDATE_OK', r)
        r = client.put(f'/api/cart/{first_cart_id}', json={'quantity': 0});
        p('CART_UPDATE_BAD', r)
    # 删除一条
    if first_cart_id:
        r = client.delete(f'/api/cart/{first_cart_id}');
        p('CART_DELETE', r)
    r = client.get(f'/api/cart?user_id={user_id}');
    p('CART_LIST_AFTER_DEL', r)

    # 12. 订单 创建 / 列表 / 详情 / 404 / 空购物车
    # 需要保证购物车还有条目：加入一个
    r = client.post('/api/cart', json={'user_id': user_id, 'product_id': 2, 'quantity': 3});
    p('CART_ADD_FOR_ORDER', r)
    r = client.post('/api/orders', json={'user_id': user_id, 'contact_name': '张三', 'contact_phone': '13800138000',
                                         'order_type': 'HOTEL', 'checkin_date': '2025-11-20',
                                         'checkout_date': '2025-11-21'})
    order_create = p('ORDER_CREATE', r)
    order_id = order_create.get('data', {}).get('order_id')
    r = client.get(f'/api/orders?user_id={user_id}');
    p('ORDER_LIST', r)
    if order_id:
        r = client.get(f'/api/orders/{order_id}');
        p('ORDER_DETAIL_OK', r)
    r = client.get('/api/orders/99999');
    p('ORDER_DETAIL_404', r)
    # 空购物车下单
    r = client.post('/api/orders', json={'user_id': user_id});
    p('ORDER_CART_EMPTY', r)
    # 缺 user_id
    r = client.post('/api/orders', json={});
    p('ORDER_NO_USER', r)

    print('\n== 冒烟测试完成 ==')


if __name__ == '__main__':
    main()
