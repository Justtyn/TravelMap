# Scenic 描述语音合成工具

该工具会读取 `db/TravelMap-back.db` 中 `scenic` 表的描述字段，调用 **DashScope Qwen3-TTS** 生成语音文件，保存到本地 `audio/` 目录，并把 `audio_url` 更新为 `https://travelmap-1307490573.cos.ap-guangzhou.myqcloud.com/audio/{景点名}.xxx`。

## 准备工作
- Python 3.10+（与项目保持一致）。
- 安装依赖：
  ```bash
  pip install -r requirements.txt
  ```
- 申请 DashScope API Key，并在运行脚本前设置环境变量：
  ```bash
  export DASHSCOPE_API_KEY=sk-xxxxx
  ```
- 若需要使用新加坡地域，请把 `--api-base` 指向 `https://dashscope-intl.aliyuncs.com/api/v1`。

## 脚本位置
`scripts/generate_scenic_audio.py`

## 常用命令
- **测试版（Dry Run）**：仅检查文本与参数，验证进度条和流程，不发起 API 请求。
  ```bash
  python scripts/generate_scenic_audio.py --dry-run --limit 3
  ```
- **一键跑通**：默认对全部景点生成语音，可根据需要叠加其他选项。
  ```bash
  python scripts/generate_scenic_audio.py --skip-existing
  ```

## 主要参数
| 参数 | 说明 |
| ---- | ---- |
| `--db-path` | SQLite 数据库路径，默认 `db/TravelMap.db` |
| `--output-dir` | 音频输出目录，默认 `audio/` |
| `--voice` | DashScope 音色（如 `Cherry`、`Ethan`） |
| `--language` | 语种参数，默认 `Chinese` |
| `--model` | 模型名称，默认 `qwen3-tts-flash` |
| `--limit` | 仅处理前 N 条，便于分批测试 |
| `--skip-existing` | 已有 `audio_url` 的记录自动跳过 |
| `--dry-run` | 只打印将执行的动作，不调用 API、不开写文件/数据库 |
| `--max-chars` | 单条描述允许的最大字符数（遵循 Qwen3-TTS 计费规则），默认 600 |

脚本会自动：
1. 去除描述中的换行与空格，仅保留文字和标点。
2. 统计字符数，超过 `--max-chars` 会提示并跳过。
3. 调用 DashScope 生成语音，并下载 `.wav/.mp3/.pcm` 文件。
4. 将文件保存为 “景点名” 命名的文件（非法字符自动清理），放到 `audio/`。
5. 把数据库中的 `audio_url` 更新为 COS 地址（需自行把音频上传至 COS）。
6. 在控制台显示进度条及最终统计（成功/跳过/失败）。

## 验证步骤
1. 脚本完成后，确认 `audio/` 目录出现对应音频文件。
2. 打开 SQLite 浏览器或使用 `sqlite3` 检查 `scenic.audio_url` 是否已写入新路径。
3. 将生成的音频上传到腾讯云 COS `audio/` 目录，使数据库 URL 与实际资源保持一致。
4. 若有失败行，脚本会打印详细原因，处理后可使用 `--skip-existing` 重跑以补齐失败记录。

## 常见问题
- **缺少 dashscope 模块**：执行 `pip install "dashscope>=1.24.6"`。
- **未设置 API Key**：环境变量 `DASHSCOPE_API_KEY` 未配置或失效。
- **字符过长**：修改数据库描述或调整 `--max-chars`，确保符合 Qwen3-TTS 限制。
- **网络/下载失败**：此时脚本会回滚该条记录，修复问题后重新运行即可。***
