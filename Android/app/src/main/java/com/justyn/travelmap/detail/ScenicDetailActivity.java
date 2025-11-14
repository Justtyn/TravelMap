package com.justyn.travelmap.detail;

import android.content.DialogInterface;
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
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.imageview.ShapeableImageView;
import com.justyn.travelmap.R;
import com.justyn.travelmap.data.local.UserPreferences;
import com.justyn.travelmap.data.local.UserProfile;
import com.justyn.travelmap.data.remote.TravelRepository;
import com.justyn.travelmap.data.remote.UserCenterRepository;
import com.justyn.travelmap.model.FeedItem;
import com.justyn.travelmap.model.VisitedRecord;

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
    private CircularProgressIndicator progressIndicator;

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
        ivBanner = findViewById(R.id.ivBanner);
        tvTitle = findViewById(R.id.tvTitle);
        tvCity = findViewById(R.id.tvCity);
        tvAddress = findViewById(R.id.tvAddress);
        tvLatLng = findViewById(R.id.tvLatLng);
        tvDescription = findViewById(R.id.tvDescription);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnVisited = findViewById(R.id.btnVisited);
        progressIndicator = findViewById(R.id.detailProgress);

        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnVisited.setOnClickListener(v -> toggleVisited());
    }

    private void loadDetail() {
        setLoading(true);
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
        setLoading(true);
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
        setLoading(true);
        executor.execute(() -> {
            try {
                userCenterRepository.addVisited(profile.getId(), scenicId, rating);
                visitedRecord = userCenterRepository.getVisitedRecord(profile.getId(), scenicId);
                runOnUiThread(() -> {
                    updateButtonStates();
                    setLoading(false);
                });
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, getString(R.string.detail_visit_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void removeVisited() {
        setLoading(true);
        executor.execute(() -> {
            try {
                userCenterRepository.removeVisited(profile.getId(), scenicId);
                visitedRecord = null;
                runOnUiThread(() -> {
                    updateButtonStates();
                    setLoading(false);
                });
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(this, getString(R.string.detail_visit_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnFavorite.setEnabled(!loading);
        btnVisited.setEnabled(!loading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
