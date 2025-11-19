package com.justyn.travelmap.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 记录是否已经完成首次启动引导，只展示一次。
 */
public class IntroPreferences {

    private static final String PREF_NAME = "travelmap_intro_pref";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private final SharedPreferences preferences;

    public IntroPreferences(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean isOnboardingCompleted() {
        return preferences.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }

    public void setOnboardingCompleted(boolean completed) {
        preferences.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply();
    }
}
