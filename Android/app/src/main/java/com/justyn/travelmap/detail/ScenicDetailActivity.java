package com.justyn.travelmap.detail;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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
import com.justyn.travelmap.model.VisitedRecord;
import com.justyn.travelmap.ui.common.ImageLoader;
import com.justyn.travelmap.ui.map.MapMarkerRenderer;
import com.justyn.travelmap.ui.map.MapPrivacyHelper;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;

public class ScenicDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SCENIC_ID = "extra_scenic_id";

    private ShapeableImageView ivBanner;
    private TextView tvTitle;
    private TextView tvCity;
    private TextView tvAddress;
    private TextView tvLatLng;
    private TextView tvMapTitle;
    private TextView tvDescription;
    private MaterialButton btnFavorite;
    private MaterialButton btnVisited;
    private CircularProgressIndicator favoriteProgress;
    private CircularProgressIndicator visitedProgress;
    private ShimmerFrameLayout skeletonLayout;
    private View contentContainer;
    private View scenicMapCard;
    private MapView detailMapView;
    private AMap scenicMap;
    private Marker scenicMarker;
    private CustomTarget<Bitmap> scenicMarkerTarget;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final TravelRepository travelRepository = new TravelRepository();
    private final UserCenterRepository userCenterRepository = new UserCenterRepository();
    private UserPreferences userPreferences;
    private UserProfile profile;

    private FeedItem currentScenic;
    private boolean isFavorited;
    private VisitedRecord visitedRecord;
    private long scenicId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapPrivacyHelper.ensurePrivacyAgreement(this);
        setContentView(R.layout.activity_scenic_detail);
        scenicId = getIntent().getLongExtra(EXTRA_SCENIC_ID, -1);
        if (scenicId <= 0) {
            finish();
            return;
        }
        userPreferences = new UserPreferences(this);
        profile = userPreferences.getUserProfile();
        if (profile == null) {
            Toast.makeText(this, R.string.toast_need_login, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        initViews(savedInstanceState);
        loadDetail();
    }

    private void initViews(@Nullable Bundle savedInstanceState) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setSubtitle(R.string.detail_breadcrumb_scenic);
        ivBanner = findViewById(R.id.ivBanner);
        tvTitle = findViewById(R.id.tvTitle);
        tvCity = findViewById(R.id.tvCity);
        tvAddress = findViewById(R.id.tvAddress);
        tvLatLng = findViewById(R.id.tvLatLng);
        tvMapTitle = findViewById(R.id.tvMapTitle);
        scenicMapCard = findViewById(R.id.scenicMapCard);
        detailMapView = findViewById(R.id.detailMapView);
        if (detailMapView != null) {
            detailMapView.onCreate(savedInstanceState);
            scenicMap = detailMapView.getMap();
        }
        tvDescription = findViewById(R.id.tvDescription);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnVisited = findViewById(R.id.btnVisited);
        favoriteProgress = findViewById(R.id.favoriteProgress);
        visitedProgress = findViewById(R.id.visitedProgress);
        skeletonLayout = findViewById(R.id.scenicSkeleton);
        contentContainer = findViewById(R.id.scenicContent);

        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnVisited.setOnClickListener(v -> toggleVisited());
    }

    private void loadDetail() {
        showSkeleton(true);
        setButtonsEnabled(false);
        executor.execute(() -> {
            try {
                FeedItem detail = travelRepository.fetchScenicDetail(scenicId);
                boolean favorited = userCenterRepository.isFavorite(profile.getId(), scenicId, "SCENIC");
                VisitedRecord record = userCenterRepository.getVisitedRecord(profile.getId(), scenicId);
                runOnUiThread(() -> {
                    currentScenic = detail;
                    isFavorited = favorited;
                    visitedRecord = record;
                    bindScenic(detail);
                    updateButtonStates();
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

    private void bindScenic(FeedItem detail) {
        if (detail == null) {
            return;
        }
        tvTitle.setText(detail.getTitle());
        tvCity.setText(detail.getExtraInfo());
        if (detail.getAddress() != null) {
            tvAddress.setText(detail.getAddress());
            tvAddress.setVisibility(View.VISIBLE);
        } else {
            tvAddress.setVisibility(View.GONE);
        }
        boolean hasLatLng = detail.getLatitude() != null && detail.getLongitude() != null;
        if (hasLatLng) {
            tvLatLng.setText(String.format("%s, %s",
                    formatDouble(detail.getLatitude()),
                    formatDouble(detail.getLongitude())));
            tvLatLng.setVisibility(View.VISIBLE);
            showScenicOnMap(detail);
        } else {
            tvLatLng.setVisibility(View.GONE);
            hideMapSection();
        }
        tvDescription.setText(detail.getDescription());
        ImageLoader.load(ivBanner, detail.getImageUrl());
    }

    private String formatDouble(Double value) {
        if (value == null) {
            return "";
        }
        return String.format("%.4f", value);
    }

    private void showScenicOnMap(FeedItem detail) {
        if (detailMapView == null || detail.getLatitude() == null || detail.getLongitude() == null) {
            hideMapSection();
            return;
        }
        if (scenicMap == null) {
            scenicMap = detailMapView.getMap();
        }
        if (scenicMap == null) {
            hideMapSection();
            return;
        }
        if (tvMapTitle != null) {
            tvMapTitle.setVisibility(View.VISIBLE);
        }
        if (scenicMapCard != null) {
            scenicMapCard.setVisibility(View.VISIBLE);
        }
        LatLng latLng = new LatLng(detail.getLatitude(), detail.getLongitude());
        if (scenicMarker != null) {
            scenicMarker.remove();
        }
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .anchor(0.5f, 1f)
                .title(detail.getTitle())
                .snippet(detail.getAddress())
                .icon(MapMarkerRenderer.create(this, detail.getTitle(), null));
        scenicMarker = scenicMap.addMarker(options);
        scenicMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        loadDetailMarkerIcon(detail);
    }

    private void loadDetailMarkerIcon(FeedItem detail) {
        if (scenicMarker == null) {
            return;
        }
        if (scenicMarkerTarget != null) {
            Glide.with(this).clear(scenicMarkerTarget);
        }
        scenicMarkerTarget = new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (scenicMarker != null) {
                    scenicMarker.setIcon(MapMarkerRenderer.create(ScenicDetailActivity.this, detail.getTitle(), resource));
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                if (scenicMarker != null) {
                    scenicMarker.setIcon(MapMarkerRenderer.create(ScenicDetailActivity.this, detail.getTitle(), null));
                }
            }
        };
        Glide.with(this)
                .asBitmap()
                .load(detail.getImageUrl())
                .into(scenicMarkerTarget);
    }

    private void hideMapSection() {
        if (tvMapTitle != null) {
            tvMapTitle.setVisibility(View.GONE);
        }
        if (scenicMapCard != null) {
            scenicMapCard.setVisibility(View.GONE);
        }
        if (scenicMarker != null) {
            scenicMarker.remove();
            scenicMarker = null;
        }
        if (scenicMarkerTarget != null) {
            Glide.with(getApplicationContext()).clear(scenicMarkerTarget);
            scenicMarkerTarget = null;
        }
    }

    private void updateButtonStates() {
        btnFavorite.setText(isFavorited ? R.string.detail_favorited : R.string.detail_favorite);
        btnVisited.setText(visitedRecord != null ? R.string.detail_visited : R.string.detail_mark_visited);
    }

    private void toggleFavorite() {
        if (currentScenic == null) {
            return;
        }
        setFavoriteLoading(true);
        executor.execute(() -> {
            try {
                if (isFavorited) {
                    userCenterRepository.removeFavorite(profile.getId(), scenicId, "SCENIC");
                    isFavorited = false;
                } else {
                    userCenterRepository.addFavorite(profile.getId(), scenicId, "SCENIC");
                    isFavorited = true;
                }
                runOnUiThread(() -> {
                    updateButtonStates();
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

    private void toggleVisited() {
        if (visitedRecord != null) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.detail_cancel_visited)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> removeVisited())
                    .setNegativeButton(R.string.detail_rating_negative, null)
                    .show();
        } else {
            showRatingDialog();
        }
    }

    private void showRatingDialog() {
        String[] scores = {"1", "2", "3", "4", "5"};
        final int[] selected = {4};
        new AlertDialog.Builder(this)
                .setTitle(R.string.detail_rating_dialog_title)
                .setSingleChoiceItems(scores, selected[0], (dialog, which) -> selected[0] = which)
                .setPositiveButton(R.string.detail_rating_positive, (dialog, which) ->
                        addVisited(selected[0] + 1))
                .setNegativeButton(R.string.detail_rating_negative, null)
                .show();
    }

    private void addVisited(int rating) {
        setVisitedLoading(true);
        executor.execute(() -> {
            try {
                userCenterRepository.addVisited(profile.getId(), scenicId, rating);
                visitedRecord = userCenterRepository.getVisitedRecord(profile.getId(), scenicId);
                runOnUiThread(() -> {
                    updateButtonStates();
                    setVisitedLoading(false);
                });
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> {
                    setVisitedLoading(false);
                    Toast.makeText(this, getString(R.string.detail_visit_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void removeVisited() {
        setVisitedLoading(true);
        executor.execute(() -> {
            try {
                userCenterRepository.removeVisited(profile.getId(), scenicId);
                visitedRecord = null;
                runOnUiThread(() -> {
                    updateButtonStates();
                    setVisitedLoading(false);
                });
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> {
                    setVisitedLoading(false);
                    Toast.makeText(this, getString(R.string.detail_visit_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
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
        btnVisited.setEnabled(enabled);
    }

    private void setFavoriteLoading(boolean loading) {
        btnFavorite.setEnabled(!loading);
        favoriteProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void setVisitedLoading(boolean loading) {
        btnVisited.setEnabled(!loading);
        visitedProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (detailMapView != null) {
            detailMapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (detailMapView != null) {
            detailMapView.onPause();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (detailMapView != null) {
            detailMapView.onLowMemory();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (detailMapView != null) {
            detailMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (skeletonLayout != null) {
            skeletonLayout.stopShimmer();
        }
        if (detailMapView != null) {
            detailMapView.onDestroy();
        }
        if (scenicMarkerTarget != null) {
            Glide.with(getApplicationContext()).clear(scenicMarkerTarget);
            scenicMarkerTarget = null;
        }
        executor.shutdownNow();
    }
}
