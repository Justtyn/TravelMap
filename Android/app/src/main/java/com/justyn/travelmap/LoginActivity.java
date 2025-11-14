package com.justyn.travelmap;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    // 用户名、密码输入框引用
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    // 登录按钮、微信登录按钮、注册入口文本
    private MaterialButton btnLogin;
    private MaterialButton btnWeChatLogin;
    private TextView tvRegisterEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化视图控件
        initViews();
        // 绑定事件监听
        bindEvents();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnWeChatLogin = findViewById(R.id.btnWeChatLogin);
        tvRegisterEntry = findViewById(R.id.tvRegisterEntry);
    }

    private void bindEvents() {
        // 普通登录点击：简单校验输入是否为空，实际项目中应调用后台接口
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            if (TextUtils.isEmpty(username)) {
                Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: 在这里加入实际的登录请求逻辑（例如调用后端 API）
            Toast.makeText(this, "模拟登录成功", Toast.LENGTH_SHORT).show();
            // 登录成功后跳转主页面
            startActivity(new Intent(this, MainActivity.class));
        });

        // 微信登录按钮点击：占位逻辑，后续需集成微信开放平台 SDK
        btnWeChatLogin.setOnClickListener(v -> {
            // TODO: 集成微信 SDK 后这里调用微信登录流程
            // 例如：IWXAPI api = WXAPIFactory.createWXAPI(context, APP_ID, true); api.sendReq(req);
            Toast.makeText(this, "暂未集成微信登录，后续请接入微信 SDK", Toast.LENGTH_SHORT).show();
        });

        // 注册入口点击：跳转注册页面
        tvRegisterEntry.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }
}