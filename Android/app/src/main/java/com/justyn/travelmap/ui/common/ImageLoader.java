package com.justyn.travelmap.ui.common;

import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;
import com.justyn.travelmap.R;

/**
 * 统一的图片加载器，提供骨架占位。
 */
public final class ImageLoader {

    private ImageLoader() {
    }

    public static void load(ImageView imageView, @Nullable String url) {
        if (imageView == null) {
            return;
        }
        Shimmer shimmer = new Shimmer.ColorHighlightBuilder()
                .setBaseColor(0xFFE0E0E0)
                .setBaseAlpha(1f)
                .setHighlightColor(0xFFF5F5F5)
                .setHighlightAlpha(1f)
                .setDropoff(50f)
                .build();
        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);

        Glide.with(imageView.getContext())
                .load(url)
                .placeholder(shimmerDrawable)
                .error(R.drawable.ic_image_placeholder)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(imageView);
    }
}
