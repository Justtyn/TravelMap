# TravelMap 微信登录前端接入说明

面向 Android 客户端（`LoginActivity`）的接口指引，说明微信授权后的请求格式、期望响应以及错误处理方案。

## 1. 整体流程

1. 通过微信 OpenSDK (`SendAuth.Req`) 拉起授权，生成 `state`（建议 `UUID`）。
2. 在 `WXEntryActivity` 的 `onResp` 中拿到 `SendAuth.Resp.code` 与 `state`，校验 `state` 后交给业务层。
3. 调用 TravelMap 后端 `POST /api/auth/wechat`，仅需 body 中的 `code` 即可（可附带前端已拿到的 `nickname` / `avatar_url` 兜底）。
4. 成功响应时，将 `data.user` 写入 `UserPreferences`，逻辑与账号密码登录一致。
5. 登录失败时，依据返回的 `code/msg` 提示用户并允许重试。

## 2. HTTP 请求

- **Method**: `POST`
- **URL**: `${BASE_URL}/api/auth/wechat`
- **Headers**: `Content-Type: application/json`
- **Body 示例**

```json
{
  "code": "051yPm0w3mBQF12b8q1w3YMFx92yPm0a",
  "state": "4a6d7a4b-269c-4149-9456-52dc5cb62694",
  "nickname": "出行侠",
  "avatar_url": "https://thirdwx.qlogo.cn/mmopen/vi_32/xxxxx"
}
```

> `code` 必填；`state` 用于日志排查，后端会忽略但建议携带；`nickname` / `avatar_url` 可选，方便后端在微信 `userinfo` 接口受限时使用前端数据。

## 3. 成功响应

```json
{
  "code": 200,
  "msg": "微信登录成功",
  "data": {
    "user": {
      "id": 42,
      "login_type": "WECHAT",
      "username": null,
      "nickname": "出行侠",
      "avatar_url": "https://thirdwx.qlogo.cn/mmopen/vi_32/xxxxx",
      "phone": null,
      "email": null,
      "wx_openid": "oIXXXXXX",
      "wx_unionid": "o7XXXXXX"
    }
  }
}
```

- 拿到 `user` 后复用现有的 `handleLoginResponse` / `UserPreferences.saveUser(user)`，无需额外处理。
- `wx_openid` / `wx_unionid` 仅用于调试，可按需忽略。

## 4. 错误场景与处理

| 返回 `code` | `msg` 示例 | 对前端的建议 |
|-------------|-----------|--------------|
| 400 | `code 不能为空` | 客户端缺少参数，提示“授权信息无效，请重新登录”并重新触发 `wx.login` |
| 500 | `WECHAT_APP_ID/WECHAT_APP_SECRET 未配置` | 服务器环境问题，提示“服务暂不可用，请稍后重试”，并记录日志 |
| 502 | `微信接口调用失败：invalid code` | 转换为“微信授权已过期，请重新尝试”并重新发起授权 |

- 网络异常（超时/断网）仍然是 HTTP 层错误，客户端按照既有的 `onFailure` 分支处理。
- 若微信 SDK 回调 `errCode != ERR_OK`，在调用接口前就应直接提示：“用户取消登录”或“微信未安装”。

## 5. 其他注意事项

1. **幂等**：同一个 `code` 只能使用一次，若网络失败需重新调用 `SendAuth.Req` 获取新的 `code`。
2. **state 校验**：虽然接口目前未校验 `state`，但客户端仍应在本地校验响应的 `state`，防止回调被重放。
3. **头像/昵称更新**：后端仅在本地昵称为空或为默认值时才会覆盖，所以客户端无需关心与账号密码混用的情况。
4. **调试**：请确保测试包签名和 AppID 在微信开放平台配置一致，否则 `code` 换取 openid 会报 `invalid appid` / `invalid signature`。
5. **登出**：调用现有的 `UserPreferences.clear()` 即可，后端不需要额外接口。

如需扩展到获取手机号 / 解绑等，请提前同步后端，确保 `session_key` 的生命周期与接口保持一致。
