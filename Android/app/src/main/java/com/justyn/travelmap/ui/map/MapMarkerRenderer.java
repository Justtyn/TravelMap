package com.justyn.travelmap.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.justyn.travelmap.R;

/**
 * 将自定义布局渲染为高德地图可用的 Marker 图标。
 */
public final class MapMarkerRenderer {

    private MapMarkerRenderer() {
    }

    public static BitmapDescriptor create(Context context, @Nullable String title, @Nullable Bitmap coverBitmap) {
        if (context == null) {
            return BitmapDescriptorFactory.defaultMarker();
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        View markerView = inflater.inflate(R.layout.view_map_marker, null);
        TextView tvTitle = markerView.findViewById(R.id.tvMarkerTitle);
        ImageView ivCover = markerView.findViewById(R.id.ivMarkerImage);
        tvTitle.setText(title == null || title.isEmpty()
                ? context.getString(R.string.map_marker_default_title)
                : title);
        if (coverBitmap != null) {
            ivCover.setImageBitmap(coverBitmap);
        } else {
            ivCover.setImageResource(R.drawable.ic_image_placeholder);
        }
        int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        markerView.measure(widthSpec, heightSpec);
        int measuredWidth = markerView.getMeasuredWidth();
        int measuredHeight = markerView.getMeasuredHeight();
        markerView.layout(0, 0, measuredWidth, measuredHeight);
        Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
