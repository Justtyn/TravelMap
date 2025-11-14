package com.justyn.travelmap.detail;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
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

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private ShapeableImageView ivCover;
    private TextView tvTitle;
    private TextView tvType;
    private TextView tvPrice;
    private TextView tvStock;
    private TextView tvAddress;
    private TextView tvDesc;
    private MaterialButton btnFavorite;
    private MaterialButton btnAddCart;
    private CircularProgressIndicator progressIndicator;

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
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        ivCover = findViewById(R.id.ivCover);
        tvTitle = findViewById(R.id.tvProductTitle);
        tvType = findViewById(R.id.tvProductType);
        tvPrice = findViewById(R.id.tvProductPrice);
        tvStock = findViewById(R.id.tvProductStock);
        tvAddress = findViewById(R.id.tvProductAddress);
        tvDesc = findViewById(R.id.tvProductDesc);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnAddCart = findViewById(R.id.btnAddCart);
        progressIndicator = findViewById(R.id.productProgress);
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnAddCart.setOnClickListener(v -> addToCart());
    }

    private void loadProduct() {
        setLoading(true);
        executor.execute(() -> {
            try {
                FeedItem detail = travelRepository.fetchProductDetail(productId);
                boolean favorited = userCenterRepository.isFavorite(profile.getId(), productId, "PRODUCT");
                runOnUiThread(() -> {
                    product = detail;
                    isFavorited = favorited;
                    bindProduct(detail);
                    updateFavoriteButton();
                    setLoading(false);
                });
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> {
                    setLoading(false);
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

        Glide.with(this)
                .load(detail.getImageUrl())
                .placeholder(R.drawable.banner_placeholder)
                .error(R.drawable.banner_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivCover);
    }

    private void updateFavoriteButton() {
        btnFavorite.setText(isFavorited ? R.string.detail_favorited : R.string.detail_favorite);
    }

    private void toggleFavorite() {
        if (product == null) {
            return;
        }
        setLoading(true);
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
                    setLoading(false);
                });
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, getString(R.string.detail_favorite_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void addToCart() {
        if (product == null) {
            return;
        }
        setLoading(true);
        executor.execute(() -> {
            try {
                userCenterRepository.addToCart(profile.getId(), productId, 1);
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, R.string.detail_cart_success, Toast.LENGTH_SHORT).show();
                });
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, getString(R.string.detail_cart_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnFavorite.setEnabled(!loading);
        btnAddCart.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
