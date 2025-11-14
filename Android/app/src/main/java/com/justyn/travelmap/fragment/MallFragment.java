package com.justyn.travelmap.fragment;

import androidx.annotation.Nullable;

import com.justyn.travelmap.R;
import com.justyn.travelmap.data.remote.TravelRepository;
import com.justyn.travelmap.model.FeedItem;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * 商城：展示门票与旅行体验。
 */
public class MallFragment extends BaseFeedFragment {

    private final TravelRepository travelRepository = new TravelRepository();

    @Override
    protected List<FeedItem> loadFeedItems(@Nullable String keyword) throws IOException, JSONException {
        return travelRepository.fetchProductsByTypes(keyword, "TICKET", "TRAVEL");
    }

    @Override
    protected String getBannerTitle() {
        return getString(R.string.feed_banner_title_mall);
    }

    @Override
    protected String getBannerSubtitle() {
        return getString(R.string.feed_banner_subtitle_mall);
    }

    @Override
    protected String getEmptyMessage() {
        return getString(R.string.feed_empty_mall);
    }

    @Override
    protected String getSearchHint() {
        return getString(R.string.feed_search_hint_mall);
    }
}
