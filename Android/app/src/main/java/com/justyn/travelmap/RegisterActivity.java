package com.justyn.travelmap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Patterns;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.justyn.travelmap.data.remote.ApiResponse;
import com.justyn.travelmap.data.remote.AuthRepository;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    // 用户名、密码、确认密码、手机号、邮箱输入框引用
    private TextInputEditText etRegUsername;
    private TextInputEditText etRegPassword;
    private TextInputEditText etRegConfirmPassword;
    private TextInputEditText etRegPhone;
    private TextInputEditText etRegEmail;
    // 注册按钮与返回登录入口
    private MaterialButton btnDoRegister;
    private TextView tvBackToLogin;
    // 单线程池，顺序执行注册网络请求
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // 主线程 Handler，用于切回 UI 线程更新界面
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    // 认证仓库，封装注册接口
    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // 打开沉浸式布局
        setContentView(R.layout.activity_register); // 加载注册页布局
        // 处理系统状态栏/导航栏内边距，保证沉浸式显示与内容不被遮挡
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()); // 读取系统栏安全区域
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // 为根布局增加内边距
            return insets; // 返回原始 Insets
        });

        // 初始化视图
        initViews();
        // 绑定事件
        bindEvents();
    }

    private void initViews() {
        etRegUsername = findViewById(R.id.etRegUsername); // 用户名输入
        etRegPassword = findViewById(R.id.etRegPassword); // 密码输入
        etRegConfirmPassword = findViewById(R.id.etRegConfirmPassword); // 确认密码输入
        etRegPhone = findViewById(R.id.etRegPhone); // 手机号输入
        etRegEmail = findViewById(R.id.etRegEmail); // 邮箱输入
        btnDoRegister = findViewById(R.id.btnDoRegister); // 注册按钮
        tvBackToLogin = findViewById(R.id.tvBackToLogin); // 返回登录入口
    }

    private void bindEvents() {
        btnDoRegister.setOnClickListener(v -> attemptRegister()); // 注册按钮点击

        // 返回登录入口点击：直接跳转登录
        tvBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class)); // 跳回登录页
            finish(); // 关闭注册页
        });
    }

    private void attemptRegister() {
        String username = etRegUsername.getText() != null ? etRegUsername.getText().toString().trim() : ""; // 取用户名
        String password = etRegPassword.getText() != null ? etRegPassword.getText().toString().trim() : ""; // 取密码
        String confirm = etRegConfirmPassword.getText() != null ? etRegConfirmPassword.getText().toString().trim() : ""; // 取确认密码
        String phone = etRegPhone.getText() != null ? etRegPhone.getText().toString().trim() : ""; // 取手机号
        String email = etRegEmail.getText() != null ? etRegEmail.getText().toString().trim() : ""; // 取邮箱

        // 校验必填项，缺失则提示并中断
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, getString(R.string.toast_input_username), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, getString(R.string.toast_input_phone), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, getString(R.string.toast_input_email), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.toast_input_valid_email), Toast.LENGTH_SHORT).show();
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

        setRegisterInProgress(true); // 提交中禁用按钮
        executor.execute(() -> {
            try {
                ApiResponse response = authRepository.register(username, password, phone, email, username); // 调用注册接口
                mainHandler.post(() -> handleRegisterResponse(response)); // 主线程处理结果
            } catch (IOException e) {
                mainHandler.post(() -> {
                    setRegisterInProgress(false); // 网络异常恢复按钮
                    showRegisterError(getString(R.string.toast_network_error)); // 提示网络错误
                });
            } catch (JSONException e) {
                mainHandler.post(() -> {
                    setRegisterInProgress(false); // 解析异常恢复按钮
                    showRegisterError(e.getMessage()); // 提示具体错误
                });
            }
        });
    }

    private void handleRegisterResponse(ApiResponse response) {
        setRegisterInProgress(false); // 恢复按钮
        if (response.isSuccess()) {
            Toast.makeText(this, getString(R.string.toast_register_success), Toast.LENGTH_SHORT).show(); // 提示注册成功
            startActivity(new Intent(this, LoginActivity.class)); // 跳回登录页
            finish(); // 关闭注册页
        } else {
            showRegisterError(response.getMessage()); // 注册失败提示后端返回信息
        }
    }

    private void showRegisterError(String detail) {
        Toast.makeText(this, getString(R.string.toast_register_failed, detail), Toast.LENGTH_SHORT).show(); // 统一错误提示
    }

    private void setRegisterInProgress(boolean inProgress) {
        btnDoRegister.setEnabled(!inProgress); // 根据状态控制按钮可点击
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow(); // 关闭线程池，避免泄漏
    }
}
