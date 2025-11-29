package com.justyn.travelmap.data.remote;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * 认证相关接口仓库，封装登录/注册/微信登录。
 */
public class AuthRepository {

    // 底层 HTTP 客户端
    private final ApiClient apiClient;

    public AuthRepository() {
        this(new ApiClient()); // 默认创建 AriderpiClient
    }

    public AuthRepository(ApiClient apiClient) {
        this.apiClient = apiClient; // 方便注入测试替换
    }

    public ApiResponse login(String username, String password) throws IOException, JSONException {
        JSONObject payload = new JSONObject(); // 构造请求体
        payload.put("username", username); // 账号
        payload.put("password", password); // 密码
        return apiClient.post("/api/auth/login", payload); // POST 调用登录接口
    }

    public ApiResponse register(String username, String password, String phone, String email, String nickname)
            throws IOException, JSONException {
        JSONObject payload = new JSONObject(); // 构造请求体
        payload.put("username", username); // 账号
        payload.put("password", password); // 密码
        payload.put("phone", phone); // 手机号
        payload.put("email", email); // 邮箱
        if (nickname != null && !nickname.isEmpty()) {
            payload.put("nickname", nickname); // 有昵称才写入
        }
        return apiClient.post("/api/auth/register", payload); // POST 调用注册接口
    }

    public ApiResponse wechatLogin(String code, @Nullable String state) throws IOException, JSONException {
        JSONObject payload = new JSONObject(); // 构造请求体
        payload.put("code", code); // 微信授权 code
        if (state != null && !state.isEmpty()) {
            payload.put("state", state); // state 存在则携带
        }
        return apiClient.post("/api/auth/wechat", payload); // POST 调用微信登录
    }
}
