package com.justyn.travelmap.ui.map;

import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.maps.MapsInitializer;

/**
 * 高德地图/定位 SDK 的隐私合规统一入口。
 */
public final class MapPrivacyHelper {

    private static final String TAG = "MapPrivacyHelper";

    private MapPrivacyHelper() {
    }

    public static void ensurePrivacyAgreement(Context context) {
        if (context == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        try {
            MapsInitializer.updatePrivacyShow(appContext, true, true);
            MapsInitializer.updatePrivacyAgree(appContext, true);
            AMapLocationClient.updatePrivacyShow(appContext, true, true);
            AMapLocationClient.updatePrivacyAgree(appContext, true);
        } catch (Exception e) {
            Log.w(TAG, "ensurePrivacyAgreement failed", e);
        }
    }
}
