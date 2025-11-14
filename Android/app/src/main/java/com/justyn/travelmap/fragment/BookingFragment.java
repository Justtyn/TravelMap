package com.justyn.travelmap.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.justyn.travelmap.R;
import com.justyn.travelmap.data.remote.TravelRepository;
import com.justyn.travelmap.model.FeedItem;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * 预订：展示酒店/住宿类商品。
 */
public class BookingFragment extends BaseFeedFragment {

    private final TravelRepository travelRepository = new TravelRepository();

    @Override
    public void onFeedItemClicked(@NonNull FeedItem item) {
        navigateToProductDetail(item.getId());
    }

    @Override
    protected List<FeedItem> loadFeedItems(@Nullable String keyword) throws IOException, JSONException {
        return travelRepository.fetchProductsByTypes(keyword, "HOTEL");
    }

    @Override
    protected String getBannerTitle() {
        return getString(R.string.feed_banner_title_booking);
    }

    @Override
    protected String getBannerSubtitle() {
        return getString(R.string.feed_banner_subtitle_booking);
    }

    @Override
    protected String getEmptyMessage() {
        return getString(R.string.feed_empty_booking);
    }

    @Override
    protected String getSearchHint() {
        return getString(R.string.feed_search_hint_booking);
    }
}
