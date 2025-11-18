import os
import requests

API_KEY = "sAyj819D0XgnbH2w4Bc0OMycSP3GVCFbn1JBNvizWnrUCFdPqkd5FeMa"
HEADERS = {
    "Authorization": API_KEY
}

def fetch_image_urls(query, count=50):
    urls = []
    per_page = 30  # 每页最多请求数，根据 API 限制可能不同
    page = 1
    while len(urls) < count:
        params = {
            "query": query,
            "per_page": per_page,
            "page": page
        }
        resp = requests.get("https://api.pexels.com/v1/search", headers=HEADERS, params=params)
        if resp.status_code != 200:
            print("请求失败，状态码：", resp.status_code)
            break
        data = resp.json()
        photos = data.get("photos", [])
        if not photos:
            break
        for photo in photos:
            # 比如获取原尺寸 URL
            urls.append(photo["src"]["original"])
            if len(urls) >= count:
                break
        page += 1
    return urls

if __name__ == "__main__":
    query = "travel map"          # 你也可以替换成中文关键词、比如 “旅行地图”
    image_urls = fetch_image_urls(query, 50)
    for i, u in enumerate(image_urls, 1):
        print(f"{i}: {u}")
