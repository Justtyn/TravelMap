package com.justyn.travelmap.profile;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.justyn.travelmap.R;
import com.justyn.travelmap.data.remote.UserCenterRepository;
import com.justyn.travelmap.model.FeedItem;
import com.justyn.travelmap.model.OrderDetail;
import com.justyn.travelmap.model.OrderItemDetail;
import com.justyn.travelmap.profile.adapter.OrderItemAdapter;
import com.justyn.travelmap.ui.map.MapMarkerRenderer;
import com.justyn.travelmap.ui.map.MapPrivacyHelper;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "extra_order_id";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final UserCenterRepository repository = new UserCenterRepository();

    private NestedScrollView contentContainer;
    private CircularProgressIndicator progressIndicator;
    private TextView tvOrderStatus;
    private TextView tvOrderNo;
    private TextView tvOrderCreateTime;
    private TextView tvOrderContact;
    private TextView tvOrderPhone;
    private TextView tvOrderTotal;
    private TextView tvOrderCheckin;
    private TextView tvOrderCheckout;
    private TextView tvMapTitle;
    private TextView tvMapHint;
    private View mapCard;
    private MapView orderMapView;
    private AMap orderMap;
    private RecyclerView rvItems;
    private OrderItemAdapter orderItemAdapter;

    private final List<Marker> mapMarkers = new ArrayList<>();
    private final List<Target<?>> markerTargets = new ArrayList<>();
    private LatLngBounds.Builder boundsBuilder;
    private int boundsCount = 0;
    private LatLng lastMarkerLatLng;

    private long orderId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapPrivacyHelper.ensurePrivacyAgreement(this);
        setContentView(R.layout.activity_order_detail);
        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1L);
        if (orderId <= 0) {
            finish();
            return;
        }
        initViews(savedInstanceState);
        loadOrderDetail();
    }

    private void initViews(@Nullable Bundle savedInstanceState) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setSubtitle(R.string.orders_breadcrumb);

        contentContainer = findViewById(R.id.orderDetailContent);
        progressIndicator = findViewById(R.id.orderDetailProgress);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvOrderNo = findViewById(R.id.tvOrderNo);
        tvOrderCreateTime = findViewById(R.id.tvOrderCreateTime);
        tvOrderContact = findViewById(R.id.tvOrderContact);
        tvOrderPhone = findViewById(R.id.tvOrderPhone);
        tvOrderTotal = findViewById(R.id.tvOrderTotal);
        tvOrderCheckin = findViewById(R.id.tvOrderCheckin);
        tvOrderCheckout = findViewById(R.id.tvOrderCheckout);
        tvMapTitle = findViewById(R.id.tvMapSectionTitle);
        tvMapHint = findViewById(R.id.tvOrderMapHint);
        mapCard = findViewById(R.id.orderMapCard);
        orderMapView = findViewById(R.id.orderMapView);
        if (orderMapView != null) {
            orderMapView.onCreate(savedInstanceState);
            orderMap = orderMapView.getMap();
            if (orderMap != null) {
                orderMap.getUiSettings().setZoomControlsEnabled(true);
                orderMap.getUiSettings().setCompassEnabled(true);
            }
        }
        rvItems = findViewById(R.id.rvOrderItems);
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        orderItemAdapter = new OrderItemAdapter();
        rvItems.setAdapter(orderItemAdapter);
    }

    private void loadOrderDetail() {
        showLoading(true);
        executor.execute(() -> {
            try {
                OrderDetail detail = repository.fetchOrderDetail(orderId);
                handler.post(() -> {
                    showLoading(false);
                    if (detail == null) {
                        Toast.makeText(this,
                                getString(R.string.order_detail_load_error, getString(R.string.feed_empty_default)),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        bindDetail(detail);
                    }
                });
            } catch (IOException | JSONException e) {
                handler.post(() -> {
                    showLoading(false);
                    Toast.makeText(this, getString(R.string.order_detail_load_error, e.getMessage()), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void bindDetail(@NonNull OrderDetail detail) {
        contentContainer.setVisibility(View.VISIBLE);
        tvOrderStatus.setText(getString(R.string.order_detail_status_format, detail.getStatus()));
        tvOrderNo.setText(getString(R.string.order_detail_order_no, detail.getOrderNo()));
        tvOrderCreateTime.setText(getString(R.string.order_detail_create_time, detail.getCreateTime()));
        if (detail.getCheckinDate() != null && !detail.getCheckinDate().isEmpty()) {
            tvOrderCheckin.setVisibility(View.VISIBLE);
            tvOrderCheckin.setText(getString(R.string.order_detail_checkin, detail.getCheckinDate()));
        } else {
            tvOrderCheckin.setVisibility(View.GONE);
        }
        if (detail.getCheckoutDate() != null && !detail.getCheckoutDate().isEmpty()) {
            tvOrderCheckout.setVisibility(View.VISIBLE);
            tvOrderCheckout.setText(getString(R.string.order_detail_checkout, detail.getCheckoutDate()));
        } else {
            tvOrderCheckout.setVisibility(View.GONE);
        }
        tvOrderContact.setText(getString(R.string.order_detail_contact,
                detail.getContactName() == null ? getString(R.string.app_name) : detail.getContactName()));
        tvOrderPhone.setText(getString(R.string.order_detail_phone,
                detail.getContactPhone() == null ? "--" : detail.getContactPhone()));
        tvOrderTotal.setText(getString(R.string.order_detail_total_amount, formatCurrency(detail.getTotalPrice())));

        List<OrderItemDetail> items = detail.getItems();
        orderItemAdapter.submitList(items);
        updateMapMarkers(items);
    }

    private void updateMapMarkers(List<OrderItemDetail> items) {
        clearMapMarkers();
        if (orderMap == null || items == null || items.isEmpty()) {
            hideMapSection();
            return;
        }
        List<OrderItemDetail> validItems = new ArrayList<>();
        for (OrderItemDetail item : items) {
            FeedItem product = item.getProduct();
            if (product != null && product.getLatitude() != null && product.getLongitude() != null) {
                validItems.add(item);
            }
        }
        if (validItems.isEmpty()) {
            hideMapSection();
            return;
        }
        mapCard.setVisibility(View.VISIBLE);
        tvMapTitle.setVisibility(View.VISIBLE);
        tvMapHint.setText(R.string.detail_map_hint);
        boundsBuilder = new LatLngBounds.Builder();
        boundsCount = 0;
        for (OrderItemDetail item : validItems) {
            addMarkerForItem(item);
        }
        fitMapBounds();
    }

    private void addMarkerForItem(OrderItemDetail item) {
        if (orderMap == null) {
            return;
        }
        FeedItem product = item.getProduct();
        if (product == null || product.getLatitude() == null || product.getLongitude() == null) {
            return;
        }
        LatLng latLng = new LatLng(product.getLatitude(), product.getLongitude());
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .anchor(0.5f, 1f)
                .title(product.getTitle())
                .snippet(product.getAddress())
                .icon(MapMarkerRenderer.create(this, product.getTitle(), null));
        Marker marker = orderMap.addMarker(options);
        mapMarkers.add(marker);
        boundsBuilder.include(latLng);
        boundsCount++;
        lastMarkerLatLng = latLng;
        CustomTarget<android.graphics.Bitmap> target = new CustomTarget<android.graphics.Bitmap>() {
            @Override
            public void onResourceReady(@NonNull android.graphics.Bitmap resource, @Nullable Transition<? super android.graphics.Bitmap> transition) {
                marker.setIcon(MapMarkerRenderer.create(OrderDetailActivity.this, product.getTitle(), resource));
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                marker.setIcon(MapMarkerRenderer.create(OrderDetailActivity.this, product.getTitle(), null));
            }
        };
        markerTargets.add(target);
        Glide.with(this)
                .asBitmap()
                .load(product.getImageUrl())
                .into(target);
    }

    private void fitMapBounds() {
        if (orderMap == null || boundsBuilder == null || boundsCount == 0) {
            return;
        }
        orderMapView.post(() -> {
            try {
                if (boundsCount == 1 && lastMarkerLatLng != null) {
                    orderMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastMarkerLatLng, 13f));
                } else {
                    orderMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 80));
                }
            } catch (Exception ignored) {
            }
        });
    }

    private void hideMapSection() {
        mapCard.setVisibility(View.GONE);
        tvMapTitle.setVisibility(View.GONE);
    }

    private void clearMapMarkers() {
        for (Marker marker : mapMarkers) {
            marker.remove();
        }
        mapMarkers.clear();
        boundsBuilder = null;
        boundsCount = 0;
        lastMarkerLatLng = null;
        for (Target<?> target : markerTargets) {
            Glide.with(this).clear(target);
        }
        markerTargets.clear();
    }

    private void showLoading(boolean loading) {
        progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        contentContainer.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    private String formatCurrency(double amount) {
        if (Double.isNaN(amount)) {
            return formatCurrency(0);
        }
        return String.format(Locale.getDefault(), "¥%.2f", amount);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (orderMapView != null) {
            orderMapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (orderMapView != null) {
            orderMapView.onPause();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (orderMapView != null) {
            orderMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (orderMapView != null) {
            orderMapView.onLowMemory();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orderMapView != null) {
            orderMapView.onDestroy();
        }
        clearMapMarkers();
        executor.shutdownNow();
    }
}
