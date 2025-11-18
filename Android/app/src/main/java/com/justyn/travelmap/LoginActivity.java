package com.justyn.travelmap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.justyn.travelmap.data.local.UserPreferences;
import com.justyn.travelmap.data.remote.ApiResponse;
import com.justyn.travelmap.data.remote.AuthRepository;
import com.justyn.travelmap.wechat.WeChatLoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity implements WeChatLoginManager.Callback {

    private static final String TAG = "LoginActivity";

    // 用户名、密码输入框引用
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    // 登录按钮、微信登录按钮、注册入口文本
    private MaterialButton btnLogin;
    private MaterialButton btnWeChatLogin;
    private TextView tvRegisterEntry;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AuthRepository authRepository = new AuthRepository();
    private UserPreferences userPreferences;
    private WeChatLoginManager weChatLoginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userPreferences = new UserPreferences(this);
        if (userPreferences.hasLoggedInUser()) {
            navigateToMain();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化视图控件
        initViews();
        weChatLoginManager = WeChatLoginManager.getInstance(getApplicationContext());
        weChatLoginManager.registerCallback(this);
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
        btnLogin.setOnClickListener(v -> attemptLogin());

        // 微信登录按钮点击：通过 SDK 拉起授权
        btnWeChatLogin.setOnClickListener(v -> startWeChatLogin());

        // 注册入口点击：跳转注册页面
        tvRegisterEntry.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, getString(R.string.toast_input_username), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, getString(R.string.toast_input_password), Toast.LENGTH_SHORT).show();
            return;
        }

        setLoginInProgress(true);
        executor.execute(() -> {
            try {
                ApiResponse response = authRepository.login(username, password);
                mainHandler.post(() -> handleLoginResponse(response));
            } catch (IOException e) {
                mainHandler.post(() -> {
                    setLoginInProgress(false);
                    showLoginError(getString(R.string.toast_network_error));
                });
            } catch (JSONException e) {
                mainHandler.post(() -> {
                    setLoginInProgress(false);
                    showLoginError(e.getMessage());
                });
            }
        });
    }

    private void startWeChatLogin() {
        setLoginInProgress(true);
        boolean started = weChatLoginManager != null && weChatLoginManager.startLogin();
        if (!started) {
            setLoginInProgress(false);
        }
    }

    private void requestWechatLogin(String code, String state) {
        executor.execute(() -> {
            try {
                ApiResponse response = authRepository.wechatLogin(code, state);
                mainHandler.post(() -> handleLoginResponse(response));
            } catch (IOException e) {
                mainHandler.post(() -> {
                    setLoginInProgress(false);
                    showLoginError(getString(R.string.toast_network_error));
                });
            } catch (JSONException e) {
                mainHandler.post(() -> {
                    setLoginInProgress(false);
                    showLoginError(e.getMessage());
                });
            }
        });
    }

    private void handleLoginResponse(ApiResponse response) {
        setLoginInProgress(false);
        if (response.isSuccess()) {
            JSONObject userJson = extractUserJson(response);
            if (userJson == null) {
                showLoginError(getString(R.string.toast_missing_user_info));
                return;
            }
            Log.d(TAG, "login success user=" + userJson);
            userPreferences.saveUser(userJson);
            Toast.makeText(this, getString(R.string.toast_login_success), Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            showLoginError(response.getMessage());
        }
    }

    private JSONObject extractUserJson(ApiResponse response) {
        Object data = response.getData();
        if (data instanceof JSONObject) {
            JSONObject dataJson = (JSONObject) data;
            return dataJson.optJSONObject("user");
        }
        return null;
    }

    private void showLoginError(String detail) {
        Toast.makeText(this, getString(R.string.toast_login_failed, detail), Toast.LENGTH_SHORT).show();
    }

    private void setLoginInProgress(boolean inProgress) {
        btnLogin.setEnabled(!inProgress);
        btnWeChatLogin.setEnabled(!inProgress);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (weChatLoginManager != null) {
            weChatLoginManager.unregisterCallback(this);
        }
        executor.shutdownNow();
    }

    @Override
    public void onWeChatAuthSuccess(@NonNull String code, @NonNull String state) {
        requestWechatLogin(code, state);
    }

    @Override
    public void onWeChatAuthCanceled() {
        setLoginInProgress(false);
        Toast.makeText(this, getString(R.string.toast_wechat_auth_canceled), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWeChatAuthFailed(@NonNull String message) {
        setLoginInProgress(false);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
