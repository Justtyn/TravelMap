package com.justyn.travelmap.data.remote;

import androidx.annotation.Nullable;

import com.justyn.travelmap.model.FeedItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 聚合首页/商城/预订需要的远程数据访问。
 */
public class TravelRepository {

    private final ApiClient apiClient;

    public TravelRepository() {
        this(new ApiClient());
    }

    public TravelRepository(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<FeedItem> fetchScenicFeed(@Nullable String keyword) throws IOException, JSONException {
        Map<String, String> queries = new HashMap<>();
        if (keyword != null && !keyword.isEmpty()) {
            queries.put("keyword", keyword);
        }
        ApiResponse response = apiClient.get("/api/scenics", queries);
        ensureSuccess(response);
        Object data = response.getData();
        if (!(data instanceof JSONArray)) {
            return new ArrayList<>();
        }
        JSONArray array = (JSONArray) data;
        List<FeedItem> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject scenic = array.optJSONObject(i);
            if (scenic == null) {
                continue;
            }
            long id = scenic.optLong("id", i);
            String title = scenic.optString("name", "未知景点");
            String description = scenic.optString("description", scenic.optString("city", "精彩旅程等你探索"));
            String imageUrl = scenic.optString("cover_image");
            result.add(new FeedItem(id, title, description, imageUrl));
        }
        return result;
    }

    public List<FeedItem> fetchProductsByTypes(@Nullable String keyword, String... types)
            throws IOException, JSONException {
        List<FeedItem> merged = new ArrayList<>();
        if (types == null || types.length == 0) {
            return merged;
        }
        for (String type : types) {
            Map<String, String> queries = new HashMap<>();
            if (keyword != null && !keyword.isEmpty()) {
                queries.put("keyword", keyword);
            }
            queries.put("type", type);
            ApiResponse response = apiClient.get("/api/products", queries);
            ensureSuccess(response);
            Object data = response.getData();
            if (!(data instanceof JSONArray)) {
                continue;
            }
            JSONArray array = (JSONArray) data;
            for (int i = 0; i < array.length(); i++) {
                JSONObject product = array.optJSONObject(i);
                if (product == null) {
                    continue;
                }
                String actualType = product.optString("type");
                if (!typeMatches(actualType, type)) {
                    continue;
                }
                long id = product.optLong("id", i);
                String title = product.optString("name", "商品");
                String description = product.optString("description",
                        String.format(Locale.getDefault(), "类型：%s", actualType));
                String imageUrl = product.optString("cover_image");
                merged.add(new FeedItem(id, title, description, imageUrl));
            }
        }
        return merged;
    }

    private boolean typeMatches(String actualType, String expectedType) {
        if (actualType == null || expectedType == null) {
            return false;
        }
        return actualType.equalsIgnoreCase(expectedType);
    }

    private void ensureSuccess(ApiResponse response) throws IOException {
        if (response == null || !response.isSuccess()) {
            String message = response != null ? response.getMessage() : "未知错误";
            throw new IOException("接口调用失败：" + message);
        }
    }
}
