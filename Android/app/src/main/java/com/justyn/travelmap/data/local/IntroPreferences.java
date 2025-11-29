package com.justyn.travelmap.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 记录是否已经完成首次启动引导，只展示一次。
 */
public class IntroPreferences {

    // 存储名，区分登录态等其他偏好
    private static final String PREF_NAME = "travelmap_intro_pref";
    // 是否完成引导的布尔值
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    // 具体的 SharedPreferences 实例
    private final SharedPreferences preferences;

    public IntroPreferences(Context context) {
        // 使用私有模式获取偏好存储，保证仅当前应用可读写
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isOnboardingCompleted() {
        // 未设置时默认返回 false，表示仍需展示引导页
        return preferences.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    public void setOnboardingCompleted(boolean completed) {
        // 写入完成状态，apply 异步提交避免阻塞主线程
        preferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply();
    }
}
