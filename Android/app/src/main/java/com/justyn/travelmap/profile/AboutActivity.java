package com.justyn.travelmap.profile;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.justyn.travelmap.R;
import com.justyn.travelmap.wechat.WeChatShareHelper;

/**
 * 关于 TravelMap 页面：集中展示分享入口与项目说明。
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initToolbar();
        bindShareActions();
    }

    private void initToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setSubtitle(R.string.about_breadcrumb);
    }

    private void bindShareActions() {
        MaterialButton btnShareFriend = findViewById(R.id.btnShareFriend);
        MaterialButton btnShareTimeline = findViewById(R.id.btnShareTimeline);
        btnShareFriend.setOnClickListener(v -> WeChatShareHelper.shareProjectHomepage(this, false));
        btnShareTimeline.setOnClickListener(v -> WeChatShareHelper.shareProjectHomepage(this, true));
    }
}
