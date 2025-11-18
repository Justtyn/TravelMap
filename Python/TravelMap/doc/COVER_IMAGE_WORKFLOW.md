# Scenic Cover Image Workflow

该脚本 `util/update_scenic_covers.py` 用于批量为 `scenic` 表生成封面图、下载到 `static/cover/` 并回写 `cover_image` 字段为 `http://139.59.227.54:5001/static/cover/{文件名}`。

## 依赖

- Python 3.10+
- `requests`（项目 `requirements.txt` 已包含）
- DashScope（阿里百炼/通义千问）图像生成 API KEY  
  将其写入环境变量 `ALIYUN_QIANWEN_API_KEY` 或运行时通过 `--api-key` 传入。
- 可访问 DashScope 的公网网络权限。

> 提示：`db/main.sql` 是 schema dump，如果你只持有该 SQL，需要先导入 SQLite 生成 `.db` 文件（示例：`sqlite3 TravelMap.db < db/main.sql`）。脚本默认使用 `db/TravelMap.db`。

## 一键操作

```bash
# 批量处理所有景点（默认模式 full）
python util/update_scenic_covers.py --mode full --skip-existing
```

脚本会：

1. 按顺序读取 `scenic` 表。
2. 对每个景点生成提示词：  
   `生成一张关于「{景点名称}」的高清实拍风格展示图……`
3. 调用 DashScope `wanx-v1`（你可通过 `--model/--size/--style` 控制）。
4. 下载返回的图片到 `static/cover/{景点名}.ext`（非法字符自动替换 `_`）。
5. 更新 `cover_image` 字段为 `http://139.59.227.54:5001/static/cover/{文件名}`。

常用可选项：

- `--skip-existing`：若已有目标域名且文件存在则跳过（推荐防止重复生成）。
- `--limit 10`：分批处理。
- `--overwrite-files`：允许覆盖已有同名文件（默认会追加 `_1` 后缀）。

## 测试版本

```bash
# 仅生成一条记录（默认取第一条）
python util/update_scenic_covers.py --mode test

# 或者指定某个景点 ID
python util/update_scenic_covers.py --mode test --scenic-id 42
```

`--mode test` 会自动将 `--limit` 设为 1，可在正式跑批前验证账号、权限与存储路径。

## 其他参数

| 参数 | 说明 |
| --- | --- |
| `--db` | SQLite 文件路径，默认 `db/TravelMap.db` |
| `--cover-dir` | 图片保存目录，默认 `static/cover` |
| `--base-url` | 对外访问 URL 前缀，默认 `http://139.59.227.54:5001/static/cover/` |
| `--poll-interval / --poll-timeout` | DashScope 异步任务轮询配置 |
| `--dry-run` | 仅打印提示词/SQL，不真正调用 API |

日志示例：

```
2025-11-18 16:14:14 INFO Requesting new cover for scenic #17 «故宫博物院»
2025-11-18 16:14:45 INFO Updated scenic #17 cover -> http://139.59.227.54:5001/static/cover/故宫博物院.png (saved static/cover/故宫博物院.png)
```

生成耗时取决于 DashScope 的排队情况，4K 级别建议留出较长的 `--poll-timeout`（默认 480 秒）。
