package com.justyn.travelmap.data.local;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 简单的用户信息实体，仅包含前端展示需要的字段，可由原始 JSON 还原。
 */
public class UserProfile {

    // 用户唯一 ID
    private final long id;
    // 账号用户名
    private final String username;
    // 昵称
    private final String nickname;
    // 手机号
    private final String phone;
    // 邮箱
    private final String email;
    // 登录类型（账号/微信等）
    private final String loginType;
    // 头像 URL
    private final String avatarUrl;
    // 原始 JSON，方便后续取其他字段
    private final JSONObject rawJson;

    private UserProfile(long id,
                        String username,
                        String nickname,
                        String phone,
                        String email,
                        String loginType,
                        String avatarUrl,
                        JSONObject rawJson) {
        this.id = id; // 用户 ID
        this.username = username; // 用户名
        this.nickname = nickname; // 昵称
        this.phone = phone; // 手机号
        this.email = email; // 邮箱
        this.loginType = loginType; // 登录类型
        this.avatarUrl = avatarUrl; // 头像链接
        this.rawJson = rawJson; // 保存原始 JSON
    }

    public static UserProfile fromJson(JSONObject userJson) throws JSONException {
        if (userJson == null) {
            throw new JSONException("user json 为空"); // 传入空直接抛异常
        }
        return new UserProfile(
                userJson.optLong("id", -1), // 读取 id，默认 -1
                userJson.optString("username", ""), // 读取用户名
                userJson.optString("nickname", ""), // 读取昵称
                userJson.optString("phone", ""), // 读取手机号
                userJson.optString("email", ""), // 读取邮箱
                userJson.optString("login_type", ""), // 读取登录类型
                userJson.optString("avatar_url", ""), // 读取头像链接
                userJson // 保留原始 JSON
        );
    }

    public long getId() {
        return id; // 返回用户 ID
    }

    public String getUsername() {
        return username; // 返回用户名
    }

    public String getNickname() {
        return nickname; // 返回昵称
    }

    public String getPhone() {
        return phone; // 返回手机号
    }

    public String getEmail() {
        return email; // 返回邮箱
    }

    public String getLoginType() {
        return loginType; // 返回登录类型
    }

    public String getAvatarUrl() {
        return avatarUrl; // 返回头像链接
    }

    public JSONObject getRawJson() {
        return rawJson; // 返回原始 JSON
    }

    @Nullable
    public String getDisplayName() {
        return nickname != null && !nickname.isEmpty() ? nickname : username; // 昵称为空时回退用用户名
    }
}
