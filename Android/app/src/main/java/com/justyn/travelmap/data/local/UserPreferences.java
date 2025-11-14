package com.justyn.travelmap.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 本地保存用户登录态的简单封装，避免在各个页面重复操作 SharedPreferences。
 */
public class UserPreferences {

    private static final String PREF_NAME = "travelmap_user_pref";
    private static final String KEY_USER_JSON = "key_user_json";

    private final SharedPreferences preferences;

    public UserPreferences(Context context) {
        Context appContext = context.getApplicationContext();
        this.preferences = appContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUser(JSONObject userJson) {
        if (userJson == null) {
            return;
        }
        preferences.edit()
                .putString(KEY_USER_JSON, userJson.toString())
                .apply();
    }

    public boolean hasLoggedInUser() {
        return preferences.contains(KEY_USER_JSON);
    }

    public UserProfile getUserProfile() {
        String rawUser = preferences.getString(KEY_USER_JSON, null);
        if (rawUser == null) {
            return null;
        }
        try {
            return UserProfile.fromJson(new JSONObject(rawUser));
        } catch (JSONException e) {
            return null;
        }
    }

    public void clear() {
        preferences.edit().remove(KEY_USER_JSON).apply();
    }
}
