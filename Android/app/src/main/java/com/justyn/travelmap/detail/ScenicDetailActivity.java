package com.justyn.travelmap.detail;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.slider.Slider;
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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;

public class ScenicDetailActivity extends AppCompatActivity {

    public static final String EXTRA_SCENIC_ID = "extra_scenic_id";
    private static final String TAG = "ScenicDetail";

    private ShapeableImageView ivBanner;
    private TextView tvTitle;
    private TextView tvCity;
    private TextView tvAddress;
    private TextView tvLatLng;
    private TextView tvMapTitle;
    private TextView tvDescription;
    private MaterialButton btnFavorite;
    private MaterialButton btnVisited;
    private MaterialButton btnAudioPlayPause;
    private MaterialButton btnAudioSpeed;
    private CircularProgressIndicator favoriteProgress;
    private CircularProgressIndicator visitedProgress;
    private ShimmerFrameLayout skeletonLayout;
    private View contentContainer;
    private View scenicMapCard;
    private MaterialCardView audioGuideCard;
    private Slider audioSlider;
    private TextView tvAudioTime;
    private LinearProgressIndicator audioLoadingBar;
    private View audioControlsGroup;
    private TextView tvAudioUnavailable;
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
    private MediaPlayer audioPlayer;
    private boolean isAudioPrepared;
    private String scenicAudioUrl;
    private final Handler audioHandler = new Handler(Looper.getMainLooper());
    // 负责定期读取 MediaPlayer 的进度，推动播放条更新
    private final Runnable audioProgressUpdater = new Runnable() {
        @Override
        public void run() {
            if (audioPlayer == null || !isAudioPrepared) {
                return;
            }
            int position = audioPlayer.getCurrentPosition();
            int duration = audioPlayer.getDuration();
            updateAudioProgress(position, duration);
            audioHandler.postDelayed(this, 500);
        }
    };
    private static final float[] AUDIO_SPEEDS = new float[]{0.75f, 1f, 1.25f, 1.5f};
    private int audioSpeedIndex = 1;

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
        audioGuideCard = findViewById(R.id.cardAudioGuide);
        detailMapView = findViewById(R.id.detailMapView);
        if (detailMapView != null) {
            detailMapView.onCreate(savedInstanceState);
            scenicMap = detailMapView.getMap();
        }
        tvDescription = findViewById(R.id.tvDescription);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnVisited = findViewById(R.id.btnVisited);
        btnAudioPlayPause = findViewById(R.id.btnAudioPlayPause);
        btnAudioSpeed = findViewById(R.id.btnAudioSpeed);
        audioSlider = findViewById(R.id.audioSlider);
        tvAudioTime = findViewById(R.id.tvAudioTime);
        audioLoadingBar = findViewById(R.id.audioLoadingBar);
        audioControlsGroup = findViewById(R.id.audioControlsGroup);
        tvAudioUnavailable = findViewById(R.id.tvAudioUnavailable);
        favoriteProgress = findViewById(R.id.favoriteProgress);
        visitedProgress = findViewById(R.id.visitedProgress);
        skeletonLayout = findViewById(R.id.scenicSkeleton);
        contentContainer = findViewById(R.id.scenicContent);

        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnVisited.setOnClickListener(v -> toggleVisited());
        if (btnAudioPlayPause != null) {
            btnAudioPlayPause.setOnClickListener(v -> toggleAudioPlayback());
        }
        if (btnAudioSpeed != null) {
            btnAudioSpeed.setOnClickListener(v -> cycleAudioSpeed());
        }
        if (audioSlider != null) {
            audioSlider.setEnabled(false);
            audioSlider.addOnChangeListener((slider, value, fromUser) -> {
                if (!fromUser || audioPlayer == null || !isAudioPrepared) {
                    return;
                }
                audioPlayer.seekTo((int) value);
                updateAudioProgress((int) value, audioPlayer.getDuration());
            });
        }
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
        setupAudioSection(detail.getAudioUrl());
    }

    private void setupAudioSection(@Nullable String audioUrl) {
        if (audioGuideCard == null || btnAudioPlayPause == null || audioSlider == null || tvAudioTime == null) {
            return;
        }
        String sanitizedUrl = sanitizeAudioUrl(audioUrl);
        audioGuideCard.setVisibility(View.VISIBLE);
        boolean hasAudio = !TextUtils.isEmpty(sanitizedUrl);
        if (!hasAudio) {
            scenicAudioUrl = null;
            if (audioControlsGroup != null) {
                audioControlsGroup.setVisibility(View.GONE);
            }
            if (tvAudioUnavailable != null) {
                tvAudioUnavailable.setVisibility(View.VISIBLE);
            }
            releaseAudioPlayer();
            return;
        }
        scenicAudioUrl = sanitizedUrl;
        if (audioControlsGroup != null) {
            audioControlsGroup.setVisibility(View.VISIBLE);
        }
        if (tvAudioUnavailable != null) {
            tvAudioUnavailable.setVisibility(View.GONE);
        }
        tvAudioTime.setText(getString(R.string.detail_audio_time_default));
        audioSlider.setEnabled(false);
        audioSlider.setValueFrom(0f);
        audioSlider.setValueTo(1f);
        audioSlider.setValue(0f);
        btnAudioPlayPause.setEnabled(false);
        updateAudioPlayPauseUi(false);
        audioSpeedIndex = 1;
        updateAudioSpeedLabel();
        prepareAudioPlayer(sanitizedUrl);
    }

    private String formatDouble(Double value) {
        if (value == null) {
            return "";
        }
        return String.format("%.4f", value);
    }

    @Nullable
    private String sanitizeAudioUrl(@Nullable String audioUrl) {
        if (audioUrl == null) {
            return null;
        }
        String trimmed = audioUrl.trim();
        if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
            return null;
        }
        return trimmed;
    }

    private void prepareAudioPlayer(String audioUrl) {
        scenicAudioUrl = audioUrl;
        releaseAudioPlayer();
        if (TextUtils.isEmpty(audioUrl)) {
            return;
        }
        showAudioLoading(true);
        audioPlayer = new MediaPlayer();
        try {
            audioPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build());
            audioPlayer.setDataSource(audioUrl);
            audioPlayer.setOnPreparedListener(mp -> {
                isAudioPrepared = true;
                showAudioLoading(false);
                if (btnAudioPlayPause != null) {
                    btnAudioPlayPause.setEnabled(true);
                }
                if (audioSlider != null) {
                    audioSlider.setEnabled(true);
                    audioSlider.setValueFrom(0f);
                    float duration = Math.max(mp.getDuration(), 1);
                    audioSlider.setValueTo(duration);
                    audioSlider.setValue(0f);
                }
                updateAudioProgress(0, mp.getDuration());
                applyAudioSpeed();
            });
            audioPlayer.setOnCompletionListener(mp -> {
                stopAudioProgressUpdates();
                mp.seekTo(0);
                updateAudioProgress(0, mp.getDuration());
                updateAudioPlayPauseUi(false);
            });
            audioPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.w(TAG, "prepareAudioPlayer onError: what=" + what + ", extra=" + extra);
                showAudioLoading(false);
                if (btnAudioPlayPause != null) {
                    btnAudioPlayPause.setEnabled(false);
                }
                if (audioSlider != null) {
                    audioSlider.setEnabled(false);
                }
                Toast.makeText(this, getString(R.string.detail_audio_error, getString(R.string.toast_network_error)), Toast.LENGTH_SHORT).show();
                return true;
            });
            // 异步准备音频，避免阻塞主线程
            audioPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "prepareAudioPlayer: ", e);
            showAudioLoading(false);
            String reason = TextUtils.isEmpty(e.getMessage())
                    ? getString(R.string.toast_network_error)
                    : e.getMessage();
            Toast.makeText(this, getString(R.string.detail_audio_error, reason), Toast.LENGTH_SHORT).show();
            releaseAudioPlayer();
        }
    }

    private void toggleAudioPlayback() {
        if (audioPlayer == null || !isAudioPrepared) {
            if (!TextUtils.isEmpty(scenicAudioUrl)) {
                prepareAudioPlayer(scenicAudioUrl);
            }
            return;
        }
        if (audioPlayer.isPlaying()) {
            pauseAudioPlayback();
        } else {
            startAudioPlayback();
        }
    }

    private void startAudioPlayback() {
        if (audioPlayer == null || !isAudioPrepared) {
            return;
        }
        audioPlayer.start();
        startAudioProgressUpdates();
        updateAudioPlayPauseUi(true);
    }

    private void pauseAudioPlayback() {
        if (audioPlayer == null || !isAudioPrepared) {
            return;
        }
        if (audioPlayer.isPlaying()) {
            audioPlayer.pause();
        }
        stopAudioProgressUpdates();
        updateAudioPlayPauseUi(false);
    }

    private void releaseAudioPlayer() {
        stopAudioProgressUpdates();
        if (audioPlayer != null) {
            try {
                audioPlayer.stop();
            } catch (IllegalStateException ignored) {
            }
            audioPlayer.release();
            audioPlayer = null;
        }
        isAudioPrepared = false;
        if (btnAudioPlayPause != null) {
            btnAudioPlayPause.setEnabled(false);
            updateAudioPlayPauseUi(false);
        }
        if (audioSlider != null) {
            audioSlider.setEnabled(false);
            audioSlider.setValue(0f);
        }
        showAudioLoading(false);
    }

    private void cycleAudioSpeed() {
        if (btnAudioSpeed == null) {
            return;
        }
        audioSpeedIndex = (audioSpeedIndex + 1) % AUDIO_SPEEDS.length;
        updateAudioSpeedLabel();
        applyAudioSpeed();
    }

    private void applyAudioSpeed() {
        if (audioPlayer == null || !isAudioPrepared) {
            return;
        }
        try {
            audioPlayer.setPlaybackParams(audioPlayer.getPlaybackParams().setSpeed(AUDIO_SPEEDS[audioSpeedIndex]));
        } catch (Exception e) {
            Log.w(TAG, "applyAudioSpeed failed", e);
        }
    }

    private void updateAudioSpeedLabel() {
        if (btnAudioSpeed != null) {
            btnAudioSpeed.setText(getString(R.string.detail_audio_speed_template, AUDIO_SPEEDS[audioSpeedIndex]));
        }
    }

    private void updateAudioPlayPauseUi(boolean playing) {
        if (btnAudioPlayPause == null) {
            return;
        }
        btnAudioPlayPause.setIconResource(playing ? R.drawable.ic_audio_pause : R.drawable.ic_audio_play);
        btnAudioPlayPause.setText(playing ? R.string.detail_audio_pause : R.string.detail_audio_play);
    }

    // 通过 Handler 定时刷新播放进度，保证播放条与音频保持同步
    private void startAudioProgressUpdates() {
        audioHandler.removeCallbacks(audioProgressUpdater);
        audioHandler.post(audioProgressUpdater);
    }

    private void stopAudioProgressUpdates() {
        audioHandler.removeCallbacks(audioProgressUpdater);
    }

    private void updateAudioProgress(int position, int duration) {
        if (audioSlider != null) {
            float max = Math.max(duration, 1);
            if (audioSlider.getValueTo() != max) {
                audioSlider.setValueTo(max);
            }
            audioSlider.setValue(Math.min(position, duration));
        }
        if (tvAudioTime != null) {
            tvAudioTime.setText(formatAudioTime(position, duration));
        }
    }

    private String formatAudioTime(int currentMs, int totalMs) {
        return String.format(Locale.getDefault(), "%s / %s",
                formatAudioDuration(currentMs),
                formatAudioDuration(totalMs));
    }

    private String formatAudioDuration(int milliseconds) {
        int totalSeconds = Math.max(milliseconds, 0) / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void showAudioLoading(boolean loading) {
        if (audioLoadingBar != null) {
            audioLoadingBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
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
        pauseAudioPlayback();
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
        releaseAudioPlayer();
        executor.shutdownNow();
        super.onDestroy();
    }
}
