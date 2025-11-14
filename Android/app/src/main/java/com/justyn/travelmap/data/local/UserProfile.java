package com.justyn.travelmap.data.local;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 简单的用户信息实体，仅包含前端展示需要的字段，可由原始 JSON 还原。
 */
public class UserProfile {

    private final long id;
    private final String username;
    private final String nickname;
    private final String phone;
    private final String email;
    private final String loginType;
    private final String avatarUrl;
    private final JSONObject rawJson;

    private UserProfile(long id,
                        String username,
                        String nickname,
                        String phone,
                        String email,
                        String loginType,
                        String avatarUrl,
                        JSONObject rawJson) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.phone = phone;
        this.email = email;
        this.loginType = loginType;
        this.avatarUrl = avatarUrl;
        this.rawJson = rawJson;
    }

    public static UserProfile fromJson(JSONObject userJson) throws JSONException {
        if (userJson == null) {
            throw new JSONException("user json 为空");
        }
        return new UserProfile(
                userJson.optLong("id", -1),
                userJson.optString("username", ""),
                userJson.optString("nickname", ""),
                userJson.optString("phone", ""),
                userJson.optString("email", ""),
                userJson.optString("login_type", ""),
                userJson.optString("avatar_url", ""),
                userJson
        );
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getLoginType() {
        return loginType;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public JSONObject getRawJson() {
        return rawJson;
    }

    @Nullable
    public String getDisplayName() {
        return nickname != null && !nickname.isEmpty() ? nickname : username;
    }
}
