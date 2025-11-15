package com.justyn.travelmap.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.justyn.travelmap.R;

/**
 * 地图页：负责展示高德地图与当前定位
 */
public class MapFragment extends Fragment implements AMapLocationListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;
    private static final String TAG = "MapFragment";

    private MapView mapView;
    private AMap aMap;
    private MyLocationStyle myLocationStyle;
    private AMapLocationClient locationClient;
    private AMapLocationClientOption locationOption;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = view.findViewById(R.id.mapView);
        updatePrivacyState();
        mapView.onCreate(savedInstanceState);
        initMap();
        initLocationClient();
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
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);
        }
        // 初始化 UI 设置，确保地图交互一致
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void initLocationClient() {
        if (getContext() == null) {
            return;
        }
        try {
            locationClient = new AMapLocationClient(requireContext().getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, "initLocationClient: create client failed", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), getString(R.string.map_location_failed, e.getMessage()), Toast.LENGTH_SHORT).show();
            }
            return;
        }
        locationOption = new AMapLocationClientOption();
        // 使用高精度模式，方便课堂演示
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationOption.setOnceLocation(true);
        locationClient.setLocationOption(locationOption);
        locationClient.setLocationListener(this);
    }

    private void updatePrivacyState() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        try {
            MapsInitializer.updatePrivacyShow(context, true, true);
            MapsInitializer.updatePrivacyAgree(context, true);
            AMapLocationClient.updatePrivacyShow(context, true, true);
            AMapLocationClient.updatePrivacyAgree(context, true);
        } catch (Exception e) {
            Log.w(TAG, "updatePrivacyState: failed", e);
        }
    }

    private void checkLocationPermission() {
        if (getContext() == null) {
            return;
        }
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
            // 真正发起定位请求
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
            } else {
                showPermissionDeniedTip();
            }
        }
    }

    private void showPermissionDeniedTip() {
        if (getContext() != null) {
            Toast.makeText(getContext(), R.string.map_permission_denied, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
            double lat = aMapLocation.getLatitude();
            double lng = aMapLocation.getLongitude();
            updateCamera(lat, lng);
            addLocationMarker(lat, lng);
        } else if (aMapLocation != null) {
            Log.e(TAG, "onLocationChanged error: " + aMapLocation.getErrorCode() + ", info: " + aMapLocation.getErrorInfo());
            if (getContext() != null) {
                Toast.makeText(getContext(), getString(R.string.map_location_failed, aMapLocation.getErrorInfo()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateCamera(double lat, double lng) {
        if (aMap == null) {
            return;
        }
        LatLng target = new LatLng(lat, lng);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(target, 16f));
    }

    private void addLocationMarker(double lat, double lng) {
        if (aMap == null) {
            return;
        }
        LatLng target = new LatLng(lat, lng);
        aMap.clear();
        aMap.addMarker(new MarkerOptions()
                .position(target)
                .title(getString(R.string.map_my_location))
                .snippet(lat + ", " + lng));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
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
        destroyLocation();
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
}
