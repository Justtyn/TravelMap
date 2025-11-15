package com.justyn.travelmap.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.justyn.travelmap.R;
import com.justyn.travelmap.data.remote.TravelRepository;
import com.justyn.travelmap.model.FeedItem;
import com.justyn.travelmap.detail.ScenicDetailActivity;
import com.justyn.travelmap.ui.map.MapMarkerRenderer;
import com.justyn.travelmap.ui.map.MapPrivacyHelper;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 地图页：展示景点坐标与实时定位。
 */
public class MapFragment extends Fragment implements AMapLocationListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final String TAG = "MapFragment";

    private MapView mapView;
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationOption;
    private CircularProgressIndicator mapProgress;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final TravelRepository travelRepository = new TravelRepository();
    private final List<Marker> scenicMarkers = new ArrayList<>();
    private final List<Target<Bitmap>> markerTargets = new ArrayList<>();

    private LatLngBounds.Builder boundsBuilder;
    private LatLng lastBoundsLatLng;
    private int boundsPointCount = 0;
    private boolean hasFittedInitialBounds = false;
    private boolean hasFittedWithLocation = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.mapView);
        mapProgress = view.findViewById(R.id.mapProgress);
        MapPrivacyHelper.ensurePrivacyAgreement(requireContext());
        mapView.onCreate(savedInstanceState);
        initMap();
        initLocationClient();
        loadScenicPoints();
        checkLocationPermission();
        return view;
    }

    private void initMap() {
        if (mapView == null) {
            return;
        }
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        if (myLocationStyle == null) {
            myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
            myLocationStyle.interval(10_000);
        }
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.getUiSettings().setZoomControlsEnabled(true);
        aMap.getUiSettings().setCompassEnabled(true);
        aMap.getUiSettings().setScaleControlsEnabled(true);
        aMap.setOnMapLoadedListener(() -> fitCameraToBounds(false, false));
        aMap.setOnMarkerClickListener(marker -> {
            Object tag = marker.getObject();
            if (tag instanceof FeedItem) {
                FeedItem scenic = (FeedItem) tag;
                openScenicDetail(scenic.getId());
                return true;
            }
            return false;
        });
    }

    private void initLocationClient() {
        try {
            locationClient = new AMapLocationClient(requireContext().getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, "initLocationClient: create client failed", e);
            Toast.makeText(requireContext(), getString(R.string.map_location_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
            return;
        }
        locationOption = new AMapLocationClientOption();
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationOption.setOnceLocation(false);
        locationOption.setInterval(10_000);
        locationOption.setNeedAddress(true);
        locationClient.setLocationOption(locationOption);
        locationClient.setLocationListener(this);
    }

    private void checkLocationPermission() {
        boolean fineGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (fineGranted || coarseGranted) {
            enableMyLocationLayer();
            startLocation();
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_LOCATION_PERMISSION);
        }
    }

    private void enableMyLocationLayer() {
        if (aMap == null) {
            return;
        }
        try {
            aMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            Log.w(TAG, "enableMyLocationLayer: missing permission", e);
        }
    }

    private void startLocation() {
        if (locationClient != null) {
            locationClient.startLocation();
        }
    }

    private void stopLocation() {
        if (locationClient != null) {
            locationClient.stopLocation();
        }
    }

    private void destroyLocation() {
        if (locationClient != null) {
            locationClient.onDestroy();
            locationClient = null;
        }
    }

    private void loadScenicPoints() {
        showMapLoading(true);
        executor.execute(() -> {
            try {
                List<FeedItem> scenics = travelRepository.fetchScenicMapPoints();
                mainHandler.post(() -> {
                    showMapLoading(false);
                    renderScenicMarkers(scenics);
                });
            } catch (IOException | JSONException e) {
                mainHandler.post(() -> {
                    showMapLoading(false);
                    if (isAdded()) {
                        Toast.makeText(requireContext(), getString(R.string.map_points_error, e.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showMapLoading(boolean show) {
        if (mapProgress == null) {
            return;
        }
        mapProgress.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void renderScenicMarkers(@Nullable List<FeedItem> scenics) {
        if (aMap == null || !isAdded()) {
            return;
        }
        clearScenicMarkers();
        if (scenics == null || scenics.isEmpty()) {
            Toast.makeText(requireContext(), R.string.map_points_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        for (FeedItem scenic : scenics) {
            addScenicMarker(scenic);
        }
        fitCameraToBounds(false, false);
    }

    private void clearScenicMarkers() {
        for (Marker marker : scenicMarkers) {
            marker.remove();
        }
        scenicMarkers.clear();
        boundsBuilder = null;
        boundsPointCount = 0;
        lastBoundsLatLng = null;
        hasFittedInitialBounds = false;
        hasFittedWithLocation = false;
        for (Target<Bitmap> target : markerTargets) {
            try {
                Glide.with(this).clear(target);
            } catch (IllegalStateException ignored) {
            }
        }
        markerTargets.clear();
    }

    private void addScenicMarker(FeedItem scenic) {
        if (aMap == null || scenic == null) {
            return;
        }
        Double lat = scenic.getLatitude();
        Double lng = scenic.getLongitude();
        if (lat == null || lng == null) {
            return;
        }
        LatLng latLng = new LatLng(lat, lng);
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .anchor(0.5f, 1f)
                .title(scenic.getTitle())
                .snippet(scenic.getAddress())
                .icon(MapMarkerRenderer.create(getContext(), scenic.getTitle(), null));
        Marker marker = aMap.addMarker(options);
        scenicMarkers.add(marker);
        marker.setObject(scenic);
        includeBounds(latLng);
        loadMarkerThumbnail(marker, scenic);
    }

    private void loadMarkerThumbnail(Marker marker, FeedItem scenic) {
        if (!isAdded() || marker == null) {
            return;
        }
        CustomTarget<Bitmap> target = new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                if (!isAdded()) {
                    return;
                }
                marker.setIcon(MapMarkerRenderer.create(getContext(), scenic.getTitle(), resource));
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                if (!isAdded()) {
                    return;
                }
                marker.setIcon(MapMarkerRenderer.create(getContext(), scenic.getTitle(), null));
            }
        };
        markerTargets.add(target);
        Glide.with(this)
                .asBitmap()
                .load(scenic.getImageUrl())
                .into(target);
    }

    private void includeBounds(LatLng latLng) {
        if (latLng == null) {
            return;
        }
        if (boundsBuilder == null) {
            boundsBuilder = new LatLngBounds.Builder();
        }
        boundsBuilder.include(latLng);
        boundsPointCount++;
        lastBoundsLatLng = latLng;
    }

    private void fitCameraToBounds(boolean force, boolean locationFit) {
        if (mapView == null || aMap == null || boundsPointCount == 0) {
            return;
        }
        if (locationFit && hasFittedWithLocation) {
            return;
        }
        if (!force && hasFittedInitialBounds) {
            return;
        }
        mapView.post(() -> {
            if (locationFit && hasFittedWithLocation) {
                return;
            }
            if (!force && hasFittedInitialBounds) {
                return;
            }
            try {
                if (boundsPointCount == 1 && lastBoundsLatLng != null) {
                    aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastBoundsLatLng, 13f));
                } else if (boundsBuilder != null) {
                    aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120));
                }
                hasFittedInitialBounds = true;
                if (locationFit) {
                    hasFittedWithLocation = true;
                }
            } catch (Exception e) {
                Log.w(TAG, "fitCameraToBounds failed", e);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            boolean granted = false;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }
            if (granted) {
                enableMyLocationLayer();
                startLocation();
            } else if (isAdded()) {
                Toast.makeText(requireContext(), R.string.map_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
            includeBounds(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
            fitCameraToBounds(true, true);
        } else if (aMapLocation != null && isAdded()) {
            Log.e(TAG, "onLocationChanged error: " + aMapLocation.getErrorCode() + ", info: " + aMapLocation.getErrorInfo());
            Toast.makeText(requireContext(), getString(R.string.map_location_failed, aMapLocation.getErrorInfo()), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
        startLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        stopLocation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDestroy();
            mapView = null;
        }
        clearScenicMarkers();
        destroyLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
    }

    private void openScenicDetail(long scenicId) {
        if (!isAdded() || scenicId <= 0) {
            return;
        }
        Intent intent = new Intent(requireContext(), ScenicDetailActivity.class);
        intent.putExtra(ScenicDetailActivity.EXTRA_SCENIC_ID, scenicId);
        startActivity(intent);
    }
}
