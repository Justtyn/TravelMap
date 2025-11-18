package com.justyn.travelmap.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.justyn.travelmap.R;
import com.justyn.travelmap.data.local.UserPreferences;
import com.justyn.travelmap.data.local.UserProfile;
import com.justyn.travelmap.data.remote.UserCenterRepository;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * 用户信息编辑页面，仅允许修改手机号与邮箱。
 */
public class UserInfoActivity extends AppCompatActivity {

    private static final Pattern CN_PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private TextView tvUsername;
    private TextView tvNickname;
    private TextInputLayout tilPhone;
    private TextInputLayout tilEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etEmail;
    private MaterialButton btnSave;
    private CircularProgressIndicator progressIndicator;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final UserCenterRepository repository = new UserCenterRepository();
    private UserPreferences userPreferences;
    private UserProfile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        userPreferences = new UserPreferences(this);
        profile = userPreferences.getUserProfile();
        if (profile == null) {
            Toast.makeText(this, R.string.toast_need_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initViews();
        bindEvents();
        renderProfile();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setSubtitle(R.string.profile_breadcrumb);
        tvUsername = findViewById(R.id.tvUserInfoUsername);
        tvNickname = findViewById(R.id.tvUserInfoNickname);
        tilPhone = findViewById(R.id.tilUserPhone);
        tilEmail = findViewById(R.id.tilUserEmail);
        etPhone = findViewById(R.id.etUserPhone);
        etEmail = findViewById(R.id.etUserEmail);
        btnSave = findViewById(R.id.btnSaveProfile);
        progressIndicator = findViewById(R.id.profileProgress);
        tilPhone.setHint(getString(R.string.user_info_phone));
        tilEmail.setHint(getString(R.string.user_info_email));
    }

    private void bindEvents() {
        btnSave.setOnClickListener(v -> attemptUpdate());
    }

    private void renderProfile() {
        String usernameDisplay = safeDisplay(profile.getUsername());
        String nicknameDisplay = safeDisplay(profile.getNickname());
        tvUsername.setText(getString(R.string.user_info_username_format, usernameDisplay));
        tvNickname.setText(getString(R.string.user_info_nickname_format, nicknameDisplay));
        etPhone.setText(isNullLike(profile.getPhone()) ? "" : profile.getPhone());
        etEmail.setText(isNullLike(profile.getEmail()) ? "" : profile.getEmail());
    }

    private String safeDisplay(String value) {
        return isNullLike(value) ? getString(R.string.my_item_empty_placeholder) : value;
    }

    private boolean isNullLike(String value) {
        if (TextUtils.isEmpty(value)) {
            return true;
        }
        return "null".equalsIgnoreCase(value.trim());
    }

    private void attemptUpdate() {
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(phone) && TextUtils.isEmpty(email)) {
            tilPhone.setError(null);
            tilEmail.setError(null);
            Toast.makeText(this, R.string.user_info_empty_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateContactInput(phone, email)) {
            return;
        }
        setLoading(true);
        executor.execute(() -> {
            try {
                JSONObject data = repository.updateUserContact(profile.getId(), phone, email);
                JSONObject userJson = data != null ? data.optJSONObject("user") : null;
                if (userJson != null) {
                    userPreferences.saveUser(userJson);
                    profile = UserProfile.fromJson(userJson);
                }
                runOnUiThread(() -> {
                    setLoading(false);
                    renderProfile();
                    Toast.makeText(this, R.string.user_info_update_success, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, getString(R.string.feed_loading_error, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        btnSave.setEnabled(!loading);
        progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    /**
     * 本地校验输入的手机号和邮箱，避免无效请求。
     */
    private boolean validateContactInput(String phone, String email) {
        tilPhone.setError(null);
        tilEmail.setError(null);
        boolean valid = true;
        if (!TextUtils.isEmpty(phone) && !CN_PHONE_PATTERN.matcher(phone).matches()) {
            tilPhone.setError(getString(R.string.user_info_invalid_phone));
            valid = false;
        }
        if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.user_info_invalid_email));
            valid = false;
        }
        return valid;
    }
}
