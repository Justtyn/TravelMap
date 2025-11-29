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
import com.justyn.travelmap.data.local.IntroPreferences;
import com.justyn.travelmap.data.local.UserPreferences;
import com.justyn.travelmap.data.remote.ApiResponse;
import com.justyn.travelmap.data.remote.AuthRepository;
import com.justyn.travelmap.onboarding.OnboardingActivity;
import com.justyn.travelmap.wechat.WeChatLoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity implements WeChatLoginManager.Callback {

    private static final String TAG = "LoginActivity";

    // 输入相关控件
    // 用户名输入框
    private TextInputEditText etUsername;
    // 密码输入框
    private TextInputEditText etPassword;
    // 登录按钮
    private MaterialButton btnLogin;
    // 微信登录按钮
    private MaterialButton btnWeChatLogin;
    // 跳转注册文本
    private TextView tvRegisterEntry;
    // 单线程池，用来串行执行登录请求
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // 主线程 Handler，用于把结果切回 UI 线程
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    // 认证仓库，封装登录接口
    private final AuthRepository authRepository = new AuthRepository();
    // 本地用户偏好，用于持久化登录态
    private UserPreferences userPreferences;
    // 引导页偏好，用于判断是否展示 onboarding
    private IntroPreferences introPreferences;
    // 微信登录管理器
    private WeChatLoginManager weChatLoginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        introPreferences = new IntroPreferences(this); // 初始化引导偏好
        // 首次启动优先检查引导页是否完成，未完成则先走引导流程
        if (!introPreferences.isOnboardingCompleted()) {
            startActivity(new Intent(this, OnboardingActivity.class)); // 跳转到引导页
            finish(); // 关闭当前登录页
            return; // 直接返回不再执行后续逻辑
        }
        userPreferences = new UserPreferences(this); // 初始化用户偏好
        // 已有登录态则直接进入主界面，避免重复登录
        if (userPreferences.hasLoggedInUser()) {
            navigateToMain(); // 已登录直接跳主界面
            return; // 阻断后续初始化
        }
        EdgeToEdge.enable(this); // 开启沉浸式布局
        setContentView(R.layout.activity_login); // 设置页面布局
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars()); // 读取系统栏安全区域
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom); // 给根布局补齐内边距
            return insets; // 返回原始 Insets 交由后续处理
        });

        // 初始化视图控件
        initViews();
        weChatLoginManager = WeChatLoginManager.getInstance(getApplicationContext()); // 获取全局微信登录管理器
        weChatLoginManager.registerCallback(this); // 注册当前 Activity 为回调
        // 绑定事件监听
        bindEvents();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername); // 绑定用户名输入框
        etPassword = findViewById(R.id.etPassword); // 绑定密码输入框
        btnLogin = findViewById(R.id.btnLogin); // 绑定登录按钮
        btnWeChatLogin = findViewById(R.id.btnWeChatLogin); // 绑定微信登录按钮
        tvRegisterEntry = findViewById(R.id.tvRegisterEntry); // 绑定注册入口文本
    }

    private void bindEvents() {
        btnLogin.setOnClickListener(v -> attemptLogin()); // 普通登录点击

        // 微信登录按钮点击：通过 SDK 拉起授权
        btnWeChatLogin.setOnClickListener(v -> startWeChatLogin());

        // 注册入口点击：跳转注册页面
        tvRegisterEntry.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class)); // 跳转注册页
        });
    }

    private void attemptLogin() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : ""; // 取值为空则给默认空
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : ""; // 同上处理密码
        // 前端先做空校验，减少无效网络请求
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, getString(R.string.toast_input_username), Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, getString(R.string.toast_input_password), Toast.LENGTH_SHORT).show();
            return;
        }

        setLoginInProgress(true); // 登录中禁用按钮
        executor.execute(() -> {
            try {
                // 登录网络请求放到单线程池，避免阻塞主线程
                ApiResponse response = authRepository.login(username, password); // 调用仓库执行登录
                mainHandler.post(() -> handleLoginResponse(response)); // 回到主线程处理结果
            } catch (IOException e) {
                mainHandler.post(() -> {
                    setLoginInProgress(false); // 网络异常时恢复按钮
                    showLoginError(getString(R.string.toast_network_error)); // 提示网络错误
                });
            } catch (JSONException e) {
                mainHandler.post(() -> {
                    setLoginInProgress(false); // 解析异常也恢复按钮
                    showLoginError(e.getMessage()); // 提示具体错误信息
                });
            }
        });
    }

    private void startWeChatLogin() {
        setLoginInProgress(true); // 授权中禁用按钮
        // 通过微信 SDK 发起授权，未成功拉起则还原按钮状态
        boolean started = weChatLoginManager != null && weChatLoginManager.startLogin();
        if (!started) {
            setLoginInProgress(false); // 拉起失败立即恢复按钮
        }
    }

    private void requestWechatLogin(String code, String state) {
        executor.execute(() -> { // 微信授权成功后后台请求后端登录
            try {
                // 微信授权成功后携带 code/state 走后端换取用户信息
                ApiResponse response = authRepository.wechatLogin(code, state); // 请求后端换取登录态
                mainHandler.post(() -> handleLoginResponse(response)); // 主线程处理结果
            } catch (IOException e) {
                mainHandler.post(() -> {
                    setLoginInProgress(false); // 网络异常恢复按钮
                    showLoginError(getString(R.string.toast_network_error)); // 提示网络错误
                });
            } catch (JSONException e) {
                mainHandler.post(() -> {
                    setLoginInProgress(false); // 解析异常恢复按钮
                    showLoginError(e.getMessage()); // 提示具体错误信息
                });
            }
        });
    }

    private void handleLoginResponse(ApiResponse response) {
        setLoginInProgress(false); // 无论成功失败都恢复按钮
        if (response.isSuccess()) {
            JSONObject userJson = extractUserJson(response); // 从 data 提取 user
            if (userJson == null) {
                showLoginError(getString(R.string.toast_missing_user_info)); // 缺少用户信息直接报错
                return;
            }
            Log.d(TAG, "login success user=" + userJson); // 打印成功日志
            // 必须把完整 user JSON 写入本地，供后续登录态校验与展示
            userPreferences.saveUser(userJson); // 保存到 SharedPreferences
            Toast.makeText(this, getString(R.string.toast_login_success), Toast.LENGTH_SHORT).show(); // 弹出成功提示
            navigateToMain(); // 进入主界面
        } else {
            showLoginError(response.getMessage()); // 接口返回失败提示消息
        }
    }

    private JSONObject extractUserJson(ApiResponse response) {
        Object data = response.getData(); // 拿到 data 对象
        if (data instanceof JSONObject) { // 确认 data 是 JSON
            JSONObject dataJson = (JSONObject) data; // 强转为 JSONObject
            return dataJson.optJSONObject("user"); // 取出 user 字段
        }
        return null; // 类型不匹配直接返回 null
    }

    private void showLoginError(String detail) {
        Toast.makeText(this, getString(R.string.toast_login_failed, detail), Toast.LENGTH_SHORT).show(); // 统一的错误 toast
    }

    private void setLoginInProgress(boolean inProgress) {
        btnLogin.setEnabled(!inProgress); // 普通登录按钮可点击状态
        btnWeChatLogin.setEnabled(!inProgress); // 微信登录按钮可点击状态
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class); // 跳转
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // 清栈，防止返回登录
        startActivity(intent); // 启动主界面
        finish(); // 结束登录页
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (weChatLoginManager != null) {
            weChatLoginManager.unregisterCallback(this); // 页面销毁时取消回调
        }
        executor.shutdownNow(); // 立即关闭线程池，清理任务
    }

    @Override
    public void onWeChatAuthSuccess(@NonNull String code, @NonNull String state) {
        requestWechatLogin(code, state); // 微信授权成功后继续登录
    }

    @Override
    public void onWeChatAuthCanceled() {
        setLoginInProgress(false); // 恢复按钮
        Toast.makeText(this, getString(R.string.toast_wechat_auth_canceled), Toast.LENGTH_SHORT).show(); // 提示取消
    }

    @Override
    public void onWeChatAuthFailed(@NonNull String message) {
        setLoginInProgress(false); // 恢复按钮
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); // 提示失败原因
    }
}
