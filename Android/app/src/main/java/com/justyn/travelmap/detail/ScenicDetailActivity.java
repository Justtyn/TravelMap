package com.justyn.travelmap.detail;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.justyn.travelmap.model.VisitedRecord;
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
    private TextView tvDescription;
    private MaterialButton btnFavorite;
    private MaterialButton btnVisited;
    private CircularProgressIndicator favoriteProgress;
    private CircularProgressIndicator visitedProgress;
    private ShimmerFrameLayout skeletonLayout;
    private View contentContainer;

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
        initViews();
        loadDetail();
    }

    private void initViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        toolbar.setSubtitle(R.string.detail_breadcrumb_scenic);
        ivBanner = findViewById(R.id.ivBanner);
        tvTitle = findViewById(R.id.tvTitle);
        tvCity = findViewById(R.id.tvCity);
        tvAddress = findViewById(R.id.tvAddress);
        tvLatLng = findViewById(R.id.tvLatLng);
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
        if (detail.getLatitude() != null && detail.getLongitude() != null) {
            tvLatLng.setText(String.format("%s, %s",
                    formatDouble(detail.getLatitude()),
                    formatDouble(detail.getLongitude())));
            tvLatLng.setVisibility(View.VISIBLE);
        } else {
            tvLatLng.setVisibility(View.GONE);
        }
        tvDescription.setText(detail.getDescription());
        Glide.with(this)
                .load(detail.getImageUrl())
                .placeholder(R.drawable.banner_placeholder)
                .error(R.drawable.banner_placeholder)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivBanner);
    }

    private String formatDouble(Double value) {
        if (value == null) {
            return "";
        }
        return String.format("%.4f", value);
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
    protected void onDestroy() {
        super.onDestroy();
        if (skeletonLayout != null) {
            skeletonLayout.stopShimmer();
        }
        executor.shutdownNow();
    }
}
