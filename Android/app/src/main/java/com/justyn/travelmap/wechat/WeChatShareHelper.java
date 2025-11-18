package com.justyn.travelmap.wechat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.justyn.travelmap.BuildConfig;
import com.justyn.travelmap.R;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;

import java.io.ByteArrayOutputStream;

/**
 * 调用微信 OpenSDK 的分享工具。
 */
public class WeChatShareHelper {

    private WeChatShareHelper() {
    }

    public static void shareProjectHomepage(@NonNull Context context, boolean toTimeline) {
        IWXAPI api = WeChatLoginManager.getInstance(context.getApplicationContext()).getWxApi();
        if (api == null || BuildConfig.WECHAT_APP_ID == null || BuildConfig.WECHAT_APP_ID.isEmpty()) {
            Toast.makeText(context, R.string.toast_wechat_not_ready, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!api.isWXAppInstalled()) {
            Toast.makeText(context, R.string.toast_wechat_not_installed, Toast.LENGTH_SHORT).show();
            return;
        }
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = context.getString(R.string.share_project_url);

        WXMediaMessage message = new WXMediaMessage(webpage);
        message.title = context.getString(R.string.share_project_title);
        message.description = context.getString(R.string.share_project_desc);
        message.thumbData = buildThumbData(context);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = "travelmap_share_" + System.currentTimeMillis();
        req.message = message;
        req.scene = toTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        boolean sent = api.sendReq(req);
        Toast.makeText(
                context,
                sent ? R.string.toast_wechat_share_success : R.string.toast_wechat_share_failed,
                Toast.LENGTH_SHORT
        ).show();
    }

    private static byte[] buildThumbData(Context context) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round);
        if (bitmap == null) {
            return null;
        }
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 120, 120, true);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.PNG, 90, output);
        bitmap.recycle();
        if (scaled != bitmap) {
            scaled.recycle();
        }
        byte[] data = output.toByteArray();
        if (data.length > 128 * 1024) {
            // Wx 限制 128 KB
            return null;
        }
        return data;
    }
}
