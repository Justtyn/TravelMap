package com.justyn.travelmap.wechat;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.justyn.travelmap.BuildConfig;
import com.justyn.travelmap.R;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.util.UUID;

/**
 * 封装微信 SDK 登录流程，负责拉起授权、校验 state 并回调页面。
 */
public class WeChatLoginManager {

    public interface Callback {
        @MainThread
        void onWeChatAuthSuccess(@NonNull String code, @NonNull String state);

        @MainThread
        void onWeChatAuthCanceled();

        @MainThread
        void onWeChatAuthFailed(@NonNull String message);
    }

    private static WeChatLoginManager instance;

    private final Context appContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private IWXAPI wxapi;
    @Nullable
    private Callback callback;
    @Nullable
    private String pendingState;

    private WeChatLoginManager(Context context) {
        this.appContext = context.getApplicationContext();
        ensureApi();
    }

    public static synchronized WeChatLoginManager getInstance(@NonNull Context context) {
        if (instance == null) {
            instance = new WeChatLoginManager(context);
        }
        return instance;
    }

    public void registerCallback(@NonNull Callback callback) {
        this.callback = callback;
    }

    public void unregisterCallback(@NonNull Callback callback) {
        if (this.callback == callback) {
            this.callback = null;
        }
    }

    @Nullable
    public IWXAPI getWxApi() {
        ensureApi();
        return wxapi;
    }

    /**
     * 拉起微信授权，如果 SDK 或 AppId 未配置会直接透出错误。
     */
    public boolean startLogin() {
        ensureApi();
        if (TextUtils.isEmpty(BuildConfig.WECHAT_APP_ID)) {
            dispatchFailure(appContext.getString(R.string.toast_wechat_app_id_missing));
            return false;
        }
        if (wxapi == null) {
            dispatchFailure(appContext.getString(R.string.toast_wechat_not_ready));
            return false;
        }
        if (!wxapi.isWXAppInstalled()) {
            dispatchFailure(appContext.getString(R.string.toast_wechat_not_installed));
            return false;
        }
        SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        pendingState = UUID.randomUUID().toString();
        req.state = pendingState;
        boolean result = wxapi.sendReq(req);
        if (!result) {
            pendingState = null;
            dispatchFailure(appContext.getString(R.string.toast_wechat_start_failed));
        }
        return result;
    }

    /**
     * 由 WXEntryActivity 将回调转发到这里，再统一分发给页面。
     */
    public void handleAuthResponse(@Nullable SendAuth.Resp resp) {
        if (resp == null) {
            dispatchFailure(appContext.getString(R.string.toast_wechat_auth_error));
            return;
        }
        if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
            if (pendingState == null || !TextUtils.equals(pendingState, resp.state)) {
                dispatchFailure(appContext.getString(R.string.toast_wechat_state_mismatch));
                pendingState = null;
                return;
            }
            pendingState = null;
            Callback target = callback;
            if (target != null) {
                mainHandler.post(() -> target.onWeChatAuthSuccess(resp.code, resp.state));
            }
        } else if (resp.errCode == BaseResp.ErrCode.ERR_USER_CANCEL) {
            pendingState = null;
            Callback target = callback;
            if (target != null) {
                mainHandler.post(target::onWeChatAuthCanceled);
            }
        } else {
            pendingState = null;
            String message = appContext.getString(R.string.toast_wechat_auth_error);
            if (!TextUtils.isEmpty(resp.errStr)) {
                message = appContext.getString(R.string.toast_wechat_auth_error_with_reason, resp.errStr);
            }
            dispatchFailure(message);
        }
    }

    private void ensureApi() {
        if (wxapi != null || TextUtils.isEmpty(BuildConfig.WECHAT_APP_ID)) {
            return;
        }
        wxapi = WXAPIFactory.createWXAPI(appContext, BuildConfig.WECHAT_APP_ID, true);
        wxapi.registerApp(BuildConfig.WECHAT_APP_ID);
    }

    private void dispatchFailure(@NonNull String message) {
        Callback target = callback;
        if (target != null) {
            mainHandler.post(() -> target.onWeChatAuthFailed(message));
        }
    }
}
