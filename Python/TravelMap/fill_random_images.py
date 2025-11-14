import os
import sqlite3
import random

from image_urls import image_urls

# ====== 1. 数据库路径，根据你的项目结构来 ======
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, "db", "TravelMap.db")  # 和你 Flask 里的设置一致

print("DB_PATH =", DB_PATH)

# ====== 2. 通用函数：给某个表的某个字段随机填充图片 ======
def fill_random_image_for_table(conn, table, field, id_field="id"):
    cursor = conn.cursor()

    # 只填充为空或 NULL 的，避免覆盖你已经手动设置的
    cursor.execute(
        f'''
        SELECT {id_field}
        FROM {table}
        WHERE {field} IS NULL OR {field} = ''
        '''
    )

    rows = cursor.fetchall()
    print(f"{table}: 需要填充的行数 = {len(rows)}")

    for (row_id,) in rows:
        url = random.choice(image_urls)
        cursor.execute(
            f'UPDATE {table} SET {field} = ? WHERE {id_field} = ?',
            (url, row_id)
        )
        print(f"  -> {table}.{field} | id={row_id} | {url}")

    conn.commit()
    print(f"{table}: 填充完成\n")


def main():
    if not os.path.exists(DB_PATH):
        raise FileNotFoundError(f"数据库不存在：{DB_PATH}")

    conn = sqlite3.connect(DB_PATH)

    try:
        # scenic 表：景点封面
        fill_random_image_for_table(conn, table="scenic", field="cover_image")

        # product 表：商品封面
        fill_random_image_for_table(conn, table="product", field="cover_image")

        # user 表：用户头像
        fill_random_image_for_table(conn, table="user", field="avatar_url")

    finally:
        conn.close()
        print("全部处理完成 ✅")


if __name__ == "__main__":
    main()
