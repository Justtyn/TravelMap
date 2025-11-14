package com.justyn.travelmap.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.justyn.travelmap.LoginActivity;
import com.justyn.travelmap.R;
import com.justyn.travelmap.data.local.UserPreferences;
import com.justyn.travelmap.data.local.UserProfile;
import com.justyn.travelmap.profile.FavoritesActivity;
import com.justyn.travelmap.profile.OrdersActivity;
import com.justyn.travelmap.profile.UserInfoActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * “我的”页面：展示用户卡片、快捷菜单以及登出入口。
 */
public class MyFragment extends Fragment {

    private UserPreferences userPreferences;
    private ShapeableImageView ivAvatar;
    private TextView tvUsername;
    private TextView tvEmail;
    private MaterialButton btnLogout;
    private LinearLayout rowProfile;
    private LinearLayout rowFavorites;
    private LinearLayout rowOrders;
    private ExecutorService avatarExecutor;
    private Handler mainHandler;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        userPreferences = new UserPreferences(context);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        avatarExecutor = Executors.newSingleThreadExecutor();
        bindViews(view);
        bindEvents();
        renderUserInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        renderUserInfo();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (avatarExecutor != null && !avatarExecutor.isShutdown()) {
            avatarExecutor.shutdownNow();
            avatarExecutor = null;
        }
    }

    private void bindViews(View root) {
        ivAvatar = root.findViewById(R.id.ivAvatar);
        tvUsername = root.findViewById(R.id.tvUsername);
        tvEmail = root.findViewById(R.id.tvEmail);
        btnLogout = root.findViewById(R.id.btnLogout);
        rowProfile = root.findViewById(R.id.rowProfile);
        rowFavorites = root.findViewById(R.id.rowFavorites);
        rowOrders = root.findViewById(R.id.rowOrders);
    }

    private void bindEvents() {
        btnLogout.setOnClickListener(v -> performLogout());
        rowProfile.setOnClickListener(v -> startActivity(new Intent(requireContext(), UserInfoActivity.class)));
        rowFavorites.setOnClickListener(v -> startActivity(new Intent(requireContext(), FavoritesActivity.class)));
        rowOrders.setOnClickListener(v -> startActivity(new Intent(requireContext(), OrdersActivity.class)));
    }

    private void renderUserInfo() {
        UserProfile profile = userPreferences.getUserProfile();
        if (profile == null) {
            Toast.makeText(requireContext(), R.string.toast_need_login, Toast.LENGTH_SHORT).show();
            redirectToLogin();
            return;
        }
        tvUsername.setText(formatField(profile.getUsername()));
        tvEmail.setText(formatField(profile.getEmail()));
        loadAvatar(profile.getAvatarUrl());
    }

    private void loadAvatar(String avatarUrl) {
        if (TextUtils.isEmpty(avatarUrl)) {
            ivAvatar.setImageResource(R.drawable.ic_nav_my);
            return;
        }
        if (avatarExecutor == null || avatarExecutor.isShutdown()) {
            avatarExecutor = Executors.newSingleThreadExecutor();
        }
        avatarExecutor.execute(() -> {
            Bitmap bitmap = downloadBitmap(avatarUrl);
            mainHandler.post(() -> {
                if (!isAdded()) {
                    return;
                }
                if (bitmap != null) {
                    ivAvatar.setImageBitmap(bitmap);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_nav_my);
                }
            });
        });
    }

    private Bitmap downloadBitmap(String urlString) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoInput(true);
            connection.connect();
            inputStream = connection.getInputStream();
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void performLogout() {
        userPreferences.clear();
        Toast.makeText(requireContext(), R.string.my_logout_success, Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private String formatField(String value) {
        return TextUtils.isEmpty(value) ? getString(R.string.my_item_empty_placeholder) : value;
    }

    private void redirectToLogin() {
        Context context = requireContext();
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
