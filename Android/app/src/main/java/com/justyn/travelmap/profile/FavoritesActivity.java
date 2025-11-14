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
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.justyn.travelmap.R;
import com.justyn.travelmap.data.local.UserPreferences;
import com.justyn.travelmap.data.local.UserProfile;
import com.justyn.travelmap.data.remote.UserCenterRepository;
import com.justyn.travelmap.detail.ProductDetailActivity;
import com.justyn.travelmap.detail.ScenicDetailActivity;
import com.justyn.travelmap.model.FeedItem;
import com.justyn.travelmap.ui.feed.FeedAdapter;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritesActivity extends AppCompatActivity implements FeedAdapter.OnItemClickListener {

    private enum FavoriteTab { PRODUCT, SCENIC }

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private CircularProgressIndicator progressIndicator;
    private MaterialButtonToggleGroup toggleGroup;
    private FeedAdapter adapter;
    private FavoriteTab currentTab = FavoriteTab.PRODUCT;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final UserCenterRepository repository = new UserCenterRepository();
    private UserProfile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        profile = new UserPreferences(this).getUserProfile();
        if (profile == null) {
            Toast.makeText(this, R.string.toast_need_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initViews();
        bindEvents();
        toggleGroup.check(R.id.btnFavProducts);
        loadFavorites(FavoriteTab.PRODUCT, false);
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        swipeRefreshLayout = findViewById(R.id.favoritesSwipeRefresh);
        recyclerView = findViewById(R.id.rvFavorites);
        tvEmpty = findViewById(R.id.tvFavoritesEmpty);
        progressIndicator = findViewById(R.id.favoritesProgress);
        toggleGroup = findViewById(R.id.favoriteToggleGroup);
        adapter = new FeedAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void bindEvents() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            FavoriteTab tab = toggleGroup.getCheckedButtonId() == R.id.btnFavScenics
                    ? FavoriteTab.SCENIC : FavoriteTab.PRODUCT;
            loadFavorites(tab, true);
        });
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                FavoriteTab tab = checkedId == R.id.btnFavScenics ? FavoriteTab.SCENIC : FavoriteTab.PRODUCT;
                loadFavorites(tab, false);
            }
        });
    }

    private void loadFavorites(FavoriteTab tab, boolean fromSwipe) {
        setLoading(fromSwipe, true);
        currentTab = tab;
        executor.execute(() -> {
            try {
                List<FeedItem> items = tab == FavoriteTab.PRODUCT
                        ? repository.fetchFavoriteProducts(profile.getId())
                        : repository.fetchFavoriteScenics(profile.getId());
                mainHandler.post(() -> {
                    adapter.submitList(items);
                    tvEmpty.setVisibility(items == null || items.isEmpty() ? View.VISIBLE : View.GONE);
                    tvEmpty.setText(tab == FavoriteTab.PRODUCT
                            ? R.string.favorites_empty_products
                            : R.string.favorites_empty_scenics);
                    setLoading(fromSwipe, false);
                });
            } catch (IOException | JSONException e) {
                mainHandler.post(() -> {
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
            progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onFeedItemClicked(@NonNull FeedItem item) {
        if (currentTab == FavoriteTab.SCENIC) {
            openScenicDetail(item.getId());
        } else {
            openProductDetail(item.getId());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void openScenicDetail(long scenicId) {
        if (scenicId <= 0) {
            return;
        }
        Intent intent = new Intent(this, ScenicDetailActivity.class);
        intent.putExtra(ScenicDetailActivity.EXTRA_SCENIC_ID, scenicId);
        startActivity(intent);
    }

    private void openProductDetail(long productId) {
        if (productId <= 0) {
            return;
        }
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, productId);
        startActivity(intent);
    }
}
