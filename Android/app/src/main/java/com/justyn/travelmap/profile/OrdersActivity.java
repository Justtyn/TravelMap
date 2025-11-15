package com.justyn.travelmap.profile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.justyn.travelmap.R;
import com.justyn.travelmap.data.local.UserPreferences;
import com.justyn.travelmap.data.local.UserProfile;
import com.justyn.travelmap.data.remote.UserCenterRepository;
import com.justyn.travelmap.model.FeedItem;
import com.justyn.travelmap.ui.feed.FeedAdapter;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrdersActivity extends AppCompatActivity implements FeedAdapter.OnItemClickListener {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private View contentContainer;
    private ShimmerFrameLayout skeletonLayout;
    private FeedAdapter adapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final UserCenterRepository repository = new UserCenterRepository();
    private UserProfile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        profile = new UserPreferences(this).getUserProfile();
        if (profile == null) {
            Toast.makeText(this, R.string.toast_need_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initViews();
        bindEvents();
        loadOrders(false);
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setSubtitle(R.string.orders_breadcrumb);
        swipeRefreshLayout = findViewById(R.id.ordersSwipeRefresh);
        recyclerView = findViewById(R.id.rvOrders);
        tvEmpty = findViewById(R.id.tvOrdersEmpty);
        contentContainer = findViewById(R.id.ordersContent);
        skeletonLayout = findViewById(R.id.ordersSkeleton);
        adapter = new FeedAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void bindEvents() {
        swipeRefreshLayout.setOnRefreshListener(() -> loadOrders(true));
    }

    private void loadOrders(boolean fromSwipe) {
        setLoading(fromSwipe, true);
        executor.execute(() -> {
            try {
                List<FeedItem> items = repository.fetchOrders(profile.getId());
                handler.post(() -> {
                    adapter.submitList(items);
                    tvEmpty.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
                    setLoading(fromSwipe, false);
                });
            } catch (IOException | JSONException e) {
                handler.post(() -> {
                    setLoading(fromSwipe, false);
                    Toast.makeText(this, getString(R.string.feed_loading_error, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setLoading(boolean fromSwipe, boolean loading) {
        if (fromSwipe) {
            swipeRefreshLayout.setRefreshing(loading);
        } else {
            showSkeleton(loading);
        }
    }

    @Override
    public void onFeedItemClicked(@NonNull FeedItem item) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, item.getId());
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_STATUS, item.getDescription());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
        if (skeletonLayout != null) {
            skeletonLayout.stopShimmer();
        }
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
        } else {
            skeletonLayout.stopShimmer();
            skeletonLayout.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
        }
    }
}
