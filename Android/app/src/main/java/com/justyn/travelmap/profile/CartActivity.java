package com.justyn.travelmap.profile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.justyn.travelmap.R;
import com.justyn.travelmap.data.local.UserPreferences;
import com.justyn.travelmap.data.local.UserProfile;
import com.justyn.travelmap.data.remote.UserCenterRepository;
import com.justyn.travelmap.model.CartItem;
import com.justyn.travelmap.profile.adapter.CartAdapter;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartActionListener {

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private TextView tvTotalAmount;
    private CircularProgressIndicator progressIndicator;
    private MaterialButton btnSubmit;
    private View contentContainer;
    private ShimmerFrameLayout skeletonLayout;
    private TextInputEditText etContactName;
    private TextInputEditText etContactPhone;
    private CartAdapter adapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final UserCenterRepository repository = new UserCenterRepository();
    private List<CartItem> currentItems = new ArrayList<>();
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
        toolbar.setSubtitle(R.string.cart_breadcrumb);
        recyclerView = findViewById(R.id.rvCart);
        tvEmpty = findViewById(R.id.tvCartEmpty);
        tvTotalAmount = findViewById(R.id.tvCartTotal);
        progressIndicator = findViewById(R.id.cartProgress);
        contentContainer = findViewById(R.id.cartContent);
        skeletonLayout = findViewById(R.id.cartSkeleton);
        btnSubmit = findViewById(R.id.btnSubmitOrder);
        etContactName = findViewById(R.id.etContactName);
        etContactPhone = findViewById(R.id.etContactPhone);
        adapter = new CartAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        prefillContactInfo();
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
                    List<CartItem> safeItems = items == null ? new ArrayList<>() : items;
                    currentItems = safeItems;
                    adapter.submitList(safeItems);
                    updateCartSummary(safeItems);
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

    private void updateCartSummary(List<CartItem> items) {
        boolean empty = items == null || items.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (tvTotalAmount != null) {
            double total = calculateTotal(items);
            tvTotalAmount.setText(getString(R.string.cart_total_amount, formatCurrency(total)));
            tvTotalAmount.setVisibility(empty ? View.GONE : View.VISIBLE);
        }
        if (progressIndicator == null || progressIndicator.getVisibility() != View.VISIBLE) {
            btnSubmit.setEnabled(!empty);
        }
    }

    private double calculateTotal(List<CartItem> items) {
        if (items == null) {
            return 0;
        }
        double sum = 0;
        for (CartItem item : items) {
            double price = item.getUnitPrice();
            if (Double.isNaN(price)) {
                price = parsePriceLabel(item.getProduct() != null ? item.getProduct().getPriceLabel() : null);
            }
            if (!Double.isNaN(price)) {
                sum += price * item.getQuantity();
            }
        }
        return sum;
    }

    private double parsePriceLabel(String label) {
        if (label == null || label.isEmpty()) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(label.replace("¥", "").trim());
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    private String formatCurrency(double value) {
        return String.format(Locale.getDefault(), "¥%.2f", value);
    }

    private boolean hasCartItems() {
        return currentItems != null && !currentItems.isEmpty();
    }

    private void setProgressVisible(boolean show) {
        if (progressIndicator == null) {
            return;
        }
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setEnabled(!show);
        if (show) {
            btnSubmit.setEnabled(false);
        } else {
            btnSubmit.setEnabled(hasCartItems());
        }
    }

    private void submitOrder() {
        if (adapter.getItemCount() == 0) {
            Toast.makeText(this, R.string.cart_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        String contactName = getContactNameInput();
        String contactPhone = getContactPhoneInput();
        if (TextUtils.isEmpty(contactName)) {
            Toast.makeText(this, R.string.cart_contact_name_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(contactPhone)) {
            Toast.makeText(this, R.string.cart_contact_phone_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        setProgressVisible(true);
        executor.execute(() -> {
            try {
                org.json.JSONObject data = repository.createOrder(profile.getId(),
                        contactName,
                        contactPhone,
                        "GENERAL",
                        null,
                        null);
                handler.post(() -> {
                    setProgressVisible(false);
                    launchSuccessPage(data);
                    loadCart();
                });
            } catch (IOException | JSONException e) {
                handler.post(() -> {
                    setProgressVisible(false);
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
        showSkeleton(loading);
    }

    @Override
    public void onQuantityChanged(@NonNull CartItem item, int newQuantity) {
        if (progressIndicator != null && progressIndicator.getVisibility() == View.VISIBLE) {
            return;
        }
        if (newQuantity < 1) {
            Toast.makeText(this, R.string.cart_min_quantity, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, R.string.cart_adjusting_quantity, Toast.LENGTH_SHORT).show();
        changeQuantity(item, newQuantity);
    }

    @Override
    public void onItemDeleted(@NonNull CartItem item) {
        if (progressIndicator != null && progressIndicator.getVisibility() == View.VISIBLE) {
            return;
        }
        confirmDelete(item);
    }

    private void changeQuantity(CartItem item, int newQuantity) {
        setProgressVisible(true);
        executor.execute(() -> {
            try {
                repository.updateCartItem(item.getCartId(), newQuantity);
                handler.post(() -> {
                    setProgressVisible(false);
                    Toast.makeText(this, R.string.cart_update_success, Toast.LENGTH_SHORT).show();
                    loadCart();
                });
            } catch (IOException | JSONException e) {
                handler.post(() -> {
                    setProgressVisible(false);
                    Toast.makeText(this, getString(R.string.cart_update_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void confirmDelete(CartItem item) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.cart_delete_confirm)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteItem(item))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteItem(CartItem item) {
        setProgressVisible(true);
        executor.execute(() -> {
            try {
                repository.deleteCartItem(item.getCartId());
                handler.post(() -> {
                    setProgressVisible(false);
                    Toast.makeText(this, R.string.cart_delete_success, Toast.LENGTH_SHORT).show();
                    loadCart();
                });
            } catch (IOException | JSONException e) {
                handler.post(() -> {
                    setProgressVisible(false);
                    Toast.makeText(this, getString(R.string.cart_delete_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (skeletonLayout != null) {
            skeletonLayout.stopShimmer();
        }
        executor.shutdownNow();
    }

    private void showSkeleton(boolean show) {
        if (skeletonLayout == null || contentContainer == null) {
            return;
        }
        if (show) {
            skeletonLayout.setVisibility(View.VISIBLE);
            skeletonLayout.startShimmer();
            contentContainer.setVisibility(View.INVISIBLE);
            tvEmpty.setVisibility(View.GONE);
            btnSubmit.setEnabled(false);
        } else {
            skeletonLayout.stopShimmer();
            skeletonLayout.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
        }
    }

    private void prefillContactInfo() {
        if (profile == null) {
            return;
        }
        if (etContactName != null) {
            String defaultName = !TextUtils.isEmpty(profile.getNickname()) ? profile.getNickname() : profile.getUsername();
            etContactName.setText(defaultName);
        }
        if (etContactPhone != null) {
            etContactPhone.setText(profile.getPhone());
        }
    }

    private String getContactNameInput() {
        return etContactName != null && etContactName.getText() != null
                ? etContactName.getText().toString().trim() : "";
    }

    private String getContactPhoneInput() {
        return etContactPhone != null && etContactPhone.getText() != null
                ? etContactPhone.getText().toString().trim() : "";
    }
}
