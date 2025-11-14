package com.justyn.travelmap.fragment;

import androidx.annotation.Nullable;

import com.justyn.travelmap.R;
import com.justyn.travelmap.data.remote.TravelRepository;
import com.justyn.travelmap.model.FeedItem;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * 首页：展示景点列表。
 */
public class HomeFragment extends BaseFeedFragment {

    private final TravelRepository travelRepository = new TravelRepository();

    @Override
    protected List<FeedItem> loadFeedItems(@Nullable String keyword) throws IOException, JSONException {
        return travelRepository.fetchScenicFeed(keyword);
    }

    @Override
    protected String getBannerTitle() {
        return getString(R.string.feed_banner_title_home);
    }

    @Override
    protected String getBannerSubtitle() {
        return getString(R.string.feed_banner_subtitle_home);
    }

    @Override
    protected String getEmptyMessage() {
        return getString(R.string.feed_empty_home);
    }

    @Override
    protected String getSearchHint() {
        return getString(R.string.feed_search_hint_home);
    }
}
