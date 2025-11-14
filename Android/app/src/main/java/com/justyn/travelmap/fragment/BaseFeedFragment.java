package com.justyn.travelmap.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.justyn.travelmap.R;
import com.justyn.travelmap.model.FeedItem;
import com.justyn.travelmap.ui.feed.FeedAdapter;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 通用列表页面：顶部搜索 + Banner + Feed 列表。
 */
public abstract class BaseFeedFragment extends Fragment implements FeedAdapter.OnItemClickListener {

    private View feedRoot;
    private View headerWrapper;
    private TextInputLayout tilSearch;
    private TextInputEditText etSearch;
    private ImageView ivBanner;
    private TextView tvBannerTitle;
    private TextView tvBannerSubtitle;
    private TextView tvEmpty;
    private CircularProgressIndicator progressIndicator;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FeedAdapter feedAdapter;
    private RecyclerView recyclerView;
    private ExecutorService executorService;
    private Handler mainHandler;
    private String latestKeyword = "";
    private long lastTapTimestamp = 0L;
    private static final long DOUBLE_TAP_INTERVAL_MS = 350L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
        bindViews(view);
        setupRecyclerView(view);
        bindActions();
        applyBannerContent();
        fetchFeed(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        executorService = null;
        feedAdapter = null;
        recyclerView = null;
        headerWrapper = null;
        tilSearch = null;
        feedRoot = null;
    }

    private void bindViews(View root) {
        feedRoot = root.findViewById(R.id.feedRoot);
        headerWrapper = root.findViewById(R.id.headerWrapper);
        tilSearch = root.findViewById(R.id.tilSearch);
        etSearch = root.findViewById(R.id.etSearch);
        ivBanner = root.findViewById(R.id.ivBanner);
        tvBannerTitle = root.findViewById(R.id.tvBannerTitle);
        tvBannerSubtitle = root.findViewById(R.id.tvBannerSubtitle);
        tvEmpty = root.findViewById(R.id.tvEmpty);
        progressIndicator = root.findViewById(R.id.progressIndicator);
        swipeRefreshLayout = root.findViewById(R.id.swipeRefresh);

        if (tilSearch != null) {
            tilSearch.setHint(getSearchHint());
        } else {
            etSearch.setHint(getSearchHint());
        }
        tvEmpty.setText(getEmptyMessage());
    }

    private void setupRecyclerView(View root) {
        recyclerView = root.findViewById(R.id.rvFeed);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        feedAdapter = new FeedAdapter(this);
        recyclerView.setAdapter(feedAdapter);
    }

    private void bindActions() {
        if (tilSearch != null) {
            tilSearch.setEndIconOnClickListener(v -> {
                latestKeyword = getQueryFromInput();
                fetchFeed(false);
            });
        }
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                latestKeyword = getQueryFromInput();
                fetchFeed(false);
                return true;
            }
            return false;
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
            latestKeyword = getQueryFromInput();
            fetchFeed(true);
        });
        if (feedRoot != null) {
            feedRoot.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    clearSearchFocus();
                }
                return false;
            });
        }
        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                clearSearchFocus();
            }
            return false;
        });
        headerWrapper.setOnClickListener(v -> {
            long current = System.currentTimeMillis();
            if (current - lastTapTimestamp <= DOUBLE_TAP_INTERVAL_MS) {
                scrollToTop();
            }
            lastTapTimestamp = current;
        });
    }

    private void applyBannerContent() {
        ivBanner.setImageResource(getBannerImageRes());
        tvBannerTitle.setText(getBannerTitle());
        tvBannerSubtitle.setText(getBannerSubtitle());
    }

    private void fetchFeed(boolean fromSwipeRefresh) {
        showLoading(fromSwipeRefresh, true);
        ExecutorService executor = executorService;
        if (executor == null) {
            return;
        }
        executor.execute(() -> {
            try {
                List<FeedItem> items = loadFeedItems(latestKeyword);
                mainHandler.post(() -> {
                    updateList(items);
                    showLoading(fromSwipeRefresh, false);
                });
            } catch (IOException | JSONException e) {
                mainHandler.post(() -> {
                    updateList(new ArrayList<>());
                    showLoading(fromSwipeRefresh, false);
                    Toast.makeText(requireContext(),
                            getString(R.string.feed_loading_error, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateList(List<FeedItem> items) {
        if (feedAdapter != null) {
            feedAdapter.submitList(items);
        }
        boolean isEmpty = items == null || items.isEmpty();
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void showLoading(boolean fromSwipeRefresh, boolean visible) {
        if (fromSwipeRefresh) {
            swipeRefreshLayout.setRefreshing(visible);
        } else {
            progressIndicator.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private String getQueryFromInput() {
        CharSequence text = etSearch.getText();
        return text != null ? text.toString().trim() : "";
    }

    private void clearSearchFocus() {
        if (etSearch != null && etSearch.hasFocus()) {
            etSearch.clearFocus();
        }
        if (tilSearch != null && tilSearch.hasFocus()) {
            tilSearch.clearFocus();
        }
        View current = requireActivity().getCurrentFocus();
        if (current != null) {
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(current.getWindowToken(), 0);
            }
        }
    }

    private void scrollToTop() {
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(0);
        }
    }

    protected abstract List<FeedItem> loadFeedItems(@Nullable String keyword) throws IOException, JSONException;

    protected String getBannerTitle() {
        return getString(R.string.feed_banner_title_home);
    }

    protected String getBannerSubtitle() {
        return getString(R.string.feed_banner_subtitle_home);
    }

    @DrawableRes
    protected int getBannerImageRes() {
        return R.drawable.banner_placeholder;
    }

    protected String getEmptyMessage() {
        return getString(R.string.feed_empty_default);
    }

    protected String getSearchHint() {
        return getString(R.string.feed_search_hint_default);
    }

    @Override
    public void onFeedItemClicked(@NonNull FeedItem item) {
        Toast.makeText(requireContext(), R.string.feed_toast_feature_pending, Toast.LENGTH_SHORT).show();
    }
}
