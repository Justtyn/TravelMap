package com.justyn.travelmap.detail;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.justyn.travelmap.R;
import com.justyn.travelmap.data.local.UserPreferences;
import com.justyn.travelmap.data.local.UserProfile;
import com.justyn.travelmap.data.remote.TravelRepository;
import com.justyn.travelmap.data.remote.UserCenterRepository;
import com.justyn.travelmap.model.FeedItem;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.justyn.travelmap.ui.common.ImageLoader;


public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private MaterialToolbar toolbar;
    private ShapeableImageView ivCover;
    private TextView tvTitle;
    private TextView tvType;
    private TextView tvPrice;
    private TextView tvStock;
    private TextView tvAddress;
    private TextView tvDesc;
    private MaterialButton btnFavorite;
    private MaterialButton btnAddCart;
    private CircularProgressIndicator favoriteProgress;
    private CircularProgressIndicator cartProgress;
    private ShimmerFrameLayout skeletonLayout;
    private View contentContainer;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final TravelRepository travelRepository = new TravelRepository();
    private final UserCenterRepository userCenterRepository = new UserCenterRepository();
    private UserProfile profile;
    private FeedItem product;
    private boolean isFavorited;
    private long productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);
        if (productId <= 0) {
            finish();
            return;
        }
        UserPreferences preferences = new UserPreferences(this);
        profile = preferences.getUserProfile();
        if (profile == null) {
            Toast.makeText(this, R.string.toast_need_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initViews();
        loadProduct();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setSubtitle(R.string.detail_breadcrumb_product);
        toolbar.setSubtitle(R.string.detail_breadcrumb_product);
        ivCover = findViewById(R.id.ivCover);
        tvTitle = findViewById(R.id.tvProductTitle);
        tvType = findViewById(R.id.tvProductType);
        tvPrice = findViewById(R.id.tvProductPrice);
        tvStock = findViewById(R.id.tvProductStock);
        tvAddress = findViewById(R.id.tvProductAddress);
        tvDesc = findViewById(R.id.tvProductDesc);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnAddCart = findViewById(R.id.btnAddCart);
        favoriteProgress = findViewById(R.id.favoriteProgress);
        cartProgress = findViewById(R.id.cartProgress);
        skeletonLayout = findViewById(R.id.productSkeleton);
        contentContainer = findViewById(R.id.productContent);
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnAddCart.setOnClickListener(v -> addToCart());
    }

    private void loadProduct() {
        showSkeleton(true);
        setButtonsEnabled(false);
        executor.execute(() -> {
            try {
                FeedItem detail = travelRepository.fetchProductDetail(productId);
                boolean favorited = userCenterRepository.isFavorite(profile.getId(), productId, "PRODUCT");
                runOnUiThread(() -> {
                    product = detail;
                    isFavorited = favorited;
                    bindProduct(detail);
                    updateFavoriteButton();
                    showSkeleton(false);
                    setButtonsEnabled(true);
                });
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> {
                    showSkeleton(false);
                    Toast.makeText(this, getString(R.string.feed_loading_error, e.getMessage()), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void bindProduct(FeedItem detail) {
        if (detail == null) {
            return;
        }
        updateToolbarSubtitle(detail);
        tvTitle.setText(detail.getTitle());
        tvType.setText(detail.getExtraInfo());
        tvPrice.setText(detail.getPriceLabel());
        if (detail.getStock() != null) {
            tvStock.setVisibility(View.VISIBLE);
            tvStock.setText(getString(R.string.feed_stock_label, detail.getStock()));
        } else {
            tvStock.setVisibility(View.GONE);
        }
        if (detail.getAddress() != null) {
            tvAddress.setVisibility(View.VISIBLE);
            tvAddress.setText(detail.getAddress());
        } else {
            tvAddress.setVisibility(View.GONE);
        }
        tvDesc.setText(detail.getDescription());

        ImageLoader.load(ivCover, detail.getImageUrl());
    }

    private void updateToolbarSubtitle(FeedItem detail) {
        if (toolbar == null || detail == null) {
            return;
        }
        String type = detail.getExtraInfo();
        if (type != null && type.equalsIgnoreCase("HOTEL")) {
            toolbar.setSubtitle(R.string.detail_breadcrumb_booking);
        } else {
            toolbar.setSubtitle(R.string.detail_breadcrumb_product);
        }
    }

    private void updateFavoriteButton() {
        btnFavorite.setText(isFavorited ? R.string.detail_favorited : R.string.detail_favorite);
    }

    private void toggleFavorite() {
        if (product == null) {
            return;
        }
        setFavoriteLoading(true);
        executor.execute(() -> {
            try {
                if (isFavorited) {
                    userCenterRepository.removeFavorite(profile.getId(), productId, "PRODUCT");
                    isFavorited = false;
                } else {
                    userCenterRepository.addFavorite(profile.getId(), productId, "PRODUCT");
                    isFavorited = true;
                }
                runOnUiThread(() -> {
                    updateFavoriteButton();
                    setFavoriteLoading(false);
                });
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> {
                    setFavoriteLoading(false);
                    Toast.makeText(this, getString(R.string.detail_favorite_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void addToCart() {
        if (product == null) {
            return;
        }
        setCartLoading(true);
        executor.execute(() -> {
            try {
                userCenterRepository.addToCart(profile.getId(), productId, 1);
                runOnUiThread(() -> {
                    setCartLoading(false);
                    Toast.makeText(this, R.string.detail_cart_success, Toast.LENGTH_SHORT).show();
                });
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> {
                    setCartLoading(false);
                    Toast.makeText(this, getString(R.string.detail_cart_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showSkeleton(boolean show) {
        if (show) {
            contentContainer.setVisibility(View.INVISIBLE);
            skeletonLayout.setVisibility(View.VISIBLE);
            skeletonLayout.startShimmer();
        } else {
            skeletonLayout.stopShimmer();
            skeletonLayout.setVisibility(View.GONE);
            contentContainer.setVisibility(View.VISIBLE);
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        btnFavorite.setEnabled(enabled);
        btnAddCart.setEnabled(enabled);
    }

    private void setFavoriteLoading(boolean loading) {
        btnFavorite.setEnabled(!loading);
        favoriteProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void setCartLoading(boolean loading) {
        btnAddCart.setEnabled(!loading);
        cartProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (skeletonLayout != null) {
            skeletonLayout.stopShimmer();
        }
        executor.shutdownNow();
    }
}