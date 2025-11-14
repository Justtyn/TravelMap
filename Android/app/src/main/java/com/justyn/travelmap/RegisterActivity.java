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

    // 用户名、密码、确认密码输入框引用
    private TextInputEditText etRegUsername;
    private TextInputEditText etRegPassword;
    private TextInputEditText etRegConfirmPassword;
    private TextInputEditText etRegPhone;
    private TextInputEditText etRegEmail;
    // 注册按钮与返回登录入口
    private MaterialButton btnDoRegister;
    private TextView tvBackToLogin;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AuthRepository authRepository = new AuthRepository();

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
        etRegPhone = findViewById(R.id.etRegPhone);
        etRegEmail = findViewById(R.id.etRegEmail);
        btnDoRegister = findViewById(R.id.btnDoRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void bindEvents() {
        btnDoRegister.setOnClickListener(v -> attemptRegister());

        // 返回登录入口点击：直接跳转登录
        tvBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptRegister() {
        String username = etRegUsername.getText() != null ? etRegUsername.getText().toString().trim() : "";
        String password = etRegPassword.getText() != null ? etRegPassword.getText().toString().trim() : "";
        String confirm = etRegConfirmPassword.getText() != null ? etRegConfirmPassword.getText().toString().trim() : "";
        String phone = etRegPhone.getText() != null ? etRegPhone.getText().toString().trim() : "";
        String email = etRegEmail.getText() != null ? etRegEmail.getText().toString().trim() : "";

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

        setRegisterInProgress(true);
        executor.execute(() -> {
            try {
                ApiResponse response = authRepository.register(username, password, phone, email, username);
                mainHandler.post(() -> handleRegisterResponse(response));
            } catch (IOException e) {
                mainHandler.post(() -> {
                    setRegisterInProgress(false);
                    showRegisterError(getString(R.string.toast_network_error));
                });
            } catch (JSONException e) {
                mainHandler.post(() -> {
                    setRegisterInProgress(false);
                    showRegisterError(e.getMessage());
                });
            }
        });
    }

    private void handleRegisterResponse(ApiResponse response) {
        setRegisterInProgress(false);
        if (response.isSuccess()) {
            Toast.makeText(this, getString(R.string.toast_register_success), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            showRegisterError(response.getMessage());
        }
    }

    private void showRegisterError(String detail) {
        Toast.makeText(this, getString(R.string.toast_register_failed, detail), Toast.LENGTH_SHORT).show();
    }

    private void setRegisterInProgress(boolean inProgress) {
        btnDoRegister.setEnabled(!inProgress);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
