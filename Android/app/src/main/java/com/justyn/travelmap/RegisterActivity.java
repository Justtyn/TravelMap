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

public class RegisterActivity extends AppCompatActivity {

    // 用户名、密码、确认密码输入框引用
    private TextInputEditText etRegUsername;
    private TextInputEditText etRegPassword;
    private TextInputEditText etRegConfirmPassword;
    // 注册按钮与返回登录入口
    private MaterialButton btnDoRegister;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        // 处理系统状态栏/导航栏内边距，保证沉浸式显示与内容不被遮挡
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化视图
        initViews();
        // 绑定事件
        bindEvents();
    }

    private void initViews() {
        etRegUsername = findViewById(R.id.etRegUsername);
        etRegPassword = findViewById(R.id.etRegPassword);
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnDoRegister = findViewById(R.id.btnDoRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void bindEvents() {
        // 注册按钮点击：进行基础校验 -> 模拟注册成功 -> 跳转登录页（或主页面，视业务而定，这里返回登录方便演示）
        btnDoRegister.setOnClickListener(v -> {
            String username = etRegUsername.getText() != null ? etRegUsername.getText().toString().trim() : "";
            String password = etRegPassword.getText() != null ? etRegPassword.getText().toString().trim() : "";
            String confirm = etRegConfirmPassword.getText() != null ? etRegConfirmPassword.getText().toString().trim() : "";

            if (TextUtils.isEmpty(username)) {
                Toast.makeText(this, getString(R.string.toast_input_username), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, getString(R.string.toast_input_password), Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(confirm)) {
                Toast.makeText(this, getString(R.string.toast_input_confirm_password), Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(confirm)) {
                Toast.makeText(this, getString(R.string.toast_password_not_match), Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: 在此处调用后端注册接口，成功后再进行跳转与状态保存（例如保存 token、用户信息）
            Toast.makeText(this, getString(R.string.toast_register_success), Toast.LENGTH_SHORT).show();
            // 这里演示：注册成功返回登录页，实际项目可直接跳主页面
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // 返回登录入口点击：直接跳转登录
        tvBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}