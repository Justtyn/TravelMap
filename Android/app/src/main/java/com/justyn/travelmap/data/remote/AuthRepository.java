package com.justyn.travelmap.data.remote;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Repository responsible for auth-related API calls.
 */
public class AuthRepository {

    private final ApiClient apiClient;

    public AuthRepository() {
        this(new ApiClient());
    }

    public AuthRepository(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiResponse login(String username, String password) throws IOException, JSONException {
        JSONObject payload = new JSONObject();
        payload.put("username", username);
        payload.put("password", password);
        return apiClient.post("/api/auth/login", payload);
    }

    public ApiResponse register(String username, String password, String phone, String email, String nickname)
            throws IOException, JSONException {
        JSONObject payload = new JSONObject();
        payload.put("username", username);
        payload.put("password", password);
        payload.put("phone", phone);
        payload.put("email", email);
        if (nickname != null && !nickname.isEmpty()) {
            payload.put("nickname", nickname);
        }
        return apiClient.post("/api/auth/register", payload);
    }

    public ApiResponse wechatLogin(String code, @Nullable String state) throws IOException, JSONException {
        JSONObject payload = new JSONObject();
        payload.put("code", code);
        if (state != null && !state.isEmpty()) {
            payload.put("state", state);
        }
        return apiClient.post("/api/auth/wechat", payload);
    }
}
