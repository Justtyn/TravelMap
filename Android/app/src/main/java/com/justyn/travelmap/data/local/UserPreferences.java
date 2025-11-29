package com.justyn.travelmap.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 本地保存用户登录态封装，避免各个页面重复操作 SharedPreferences。
 */
public class UserPreferences {

    // 存储文件名，用于区分其他偏好
    private static final String PREF_NAME = "travelmap_user_pref";
    // 存储完整 user JSON 的键
    private static final String KEY_USER_JSON = "key_user_json";

    // 具体的 SharedPreferences 实例
    private final SharedPreferences preferences;

    public UserPreferences(Context context) {
        Context appContext = context.getApplicationContext(); // 拿到全局上下文，避免泄漏 Activity
        this.preferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE); // 以私有模式打开
    }

    public void saveUser(JSONObject userJson) {
        if (userJson == null) {
            return; // 传入空则不保存
        }
        preferences.edit() // 开启编辑
                .putString(KEY_USER_JSON, userJson.toString()) // 写入 JSON 字符串
                .apply(); // 提交
    }

    public boolean hasLoggedInUser() {
        return preferences.contains(KEY_USER_JSON); // 是否存在用户数据键
    }

    public UserProfile getUserProfile() {
        String rawUser = preferences.getString(KEY_USER_JSON, null); // 读取原始 JSON 字符串
        if (rawUser == null) {
            return null; // 没有数据返回空
        }
        try {
            return UserProfile.fromJson(new JSONObject(rawUser)); // 转为 JSONObject 再封装成 UserProfile
        } catch (JSONException e) {
            return null; // 解析异常返回空，调用方自行兜底
        }
    }

    public void clear() {
        preferences.edit().remove(KEY_USER_JSON).apply(); // 清空登录态
    }
}
