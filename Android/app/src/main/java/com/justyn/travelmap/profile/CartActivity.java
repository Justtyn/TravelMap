package com.justyn.travelmap.profile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.justyn.travelmap.R;
import com.justyn.travelmap.data.local.UserPreferences;
import com.justyn.travelmap.data.local.UserProfile;
import com.justyn.travelmap.data.remote.UserCenterRepository;
import com.justyn.travelmap.model.CartItem;
import com.justyn.travelmap.profile.adapter.CartAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private CircularProgressIndicator progressIndicator;
    private MaterialButton btnSubmit;
    private CartAdapter adapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final UserCenterRepository repository = new UserCenterRepository();
    private UserProfile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        profile = new UserPreferences(this).getUserProfile();
        if (profile == null) {
            Toast.makeText(this, R.string.toast_need_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initViews();
        bindEvents();
        loadCart();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        recyclerView = findViewById(R.id.rvCart);
        tvEmpty = findViewById(R.id.tvCartEmpty);
        progressIndicator = findViewById(R.id.cartProgress);
        btnSubmit = findViewById(R.id.btnSubmitOrder);
        adapter = new CartAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void bindEvents() {
        btnSubmit.setOnClickListener(v -> submitOrder());
    }

    private void loadCart() {
        setLoading(true);
        executor.execute(() -> {
            try {
                List<CartItem> items = repository.fetchCart(profile.getId());
                handler.post(() -> {
                    adapter.submitList(items);
                    boolean empty = items == null || items.isEmpty();
                    tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                    btnSubmit.setEnabled(!empty);
                    setLoading(false);
                });
            } catch (IOException | JSONException e) {
                handler.post(() -> {
                    setLoading(false);
                    Toast.makeText(this, getString(R.string.feed_loading_error, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void submitOrder() {
        if (adapter.getItemCount() == 0) {
            Toast.makeText(this, R.string.cart_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        String contactPhone = profile.getPhone();
        if (contactPhone == null || contactPhone.isEmpty()) {
            Toast.makeText(this, R.string.user_info_phone, Toast.LENGTH_SHORT).show();
            return;
        }
        btnSubmit.setEnabled(false);
        progressIndicator.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            try {
                org.json.JSONObject data = repository.createOrder(profile.getId(),
                        profile.getNickname() != null ? profile.getNickname() : profile.getUsername(),
                        contactPhone,
                        "GENERAL",
                        null,
                        null);
                handler.post(() -> {
                    progressIndicator.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    launchSuccessPage(data);
                    loadCart();
                });
            } catch (IOException | JSONException e) {
                handler.post(() -> {
                    progressIndicator.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, getString(R.string.cart_submit_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void launchSuccessPage(org.json.JSONObject data) {
        if (data == null) {
            Toast.makeText(this, R.string.cart_submit_success, Toast.LENGTH_SHORT).show();
            return;
        }
        org.json.JSONObject order = data.optJSONObject("order");
        String orderNo = order != null ? order.optString("order_no") : "";
        String price = "";
        if (order != null) {
            double total = order.optDouble("total_price", Double.NaN);
            if (!Double.isNaN(total)) {
                price = String.format("¥%.2f", total);
            }
        }
        android.content.Intent intent = new android.content.Intent(this, OrderSuccessActivity.class);
        intent.putExtra(OrderSuccessActivity.EXTRA_ORDER_NO, orderNo);
        intent.putExtra(OrderSuccessActivity.EXTRA_ORDER_PRICE, price);
        startActivity(intent);
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
