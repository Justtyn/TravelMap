package com.justyn.travelmap.data.remote;

import android.text.TextUtils;

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
        return parseScenicArray((JSONArray) data);
    }

    public List<FeedItem> fetchScenicMapPoints() throws IOException, JSONException {
        ApiResponse response = apiClient.get("/api/scenics/map");
        ensureSuccess(response);
        Object data = response.getData();
        if (!(data instanceof JSONArray)) {
            return new ArrayList<>();
        }
        return parseScenicArray((JSONArray) data);
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
                FeedItem item = buildProductItem(product);
                if (item != null) {
                    merged.add(item);
                }
            }
        }
        return merged;
    }

    public FeedItem fetchScenicDetail(long scenicId) throws IOException, JSONException {
        ApiResponse response = apiClient.get("/api/scenics/" + scenicId);
        ensureSuccess(response);
        Object data = response.getData();
        if (data instanceof JSONObject) {
            return buildScenicItem((JSONObject) data);
        }
        return null;
    }

    public FeedItem fetchProductDetail(long productId) throws IOException, JSONException {
        ApiResponse response = apiClient.get("/api/products/" + productId);
        ensureSuccess(response);
        Object data = response.getData();
        if (data instanceof JSONObject) {
            return buildProductItem((JSONObject) data);
        }
        return null;
    }

    private String formatPrice(double price) {
        if (Double.isNaN(price)) {
            return null;
        }
        return String.format(Locale.getDefault(), "¥%s", price % 1 == 0 ? String.format(Locale.getDefault(), "%.0f", price) : String.format(Locale.getDefault(), "%.2f", price));
    }

    private FeedItem buildScenicItem(JSONObject scenic) {
        if (scenic == null) {
            return null;
        }
        long id = scenic.optLong("id", 0);
        String title = scenic.optString("name", "未知景点");
        String description = scenic.optString("description", scenic.optString("city", "精彩旅程等你探索"));
        String imageUrl = scenic.optString("cover_image");
        String address = scenic.optString("address");
        Double lat = (scenic.has("latitude") && !scenic.isNull("latitude"))
                ? scenic.optDouble("latitude") : null;
        Double lng = (scenic.has("longitude") && !scenic.isNull("longitude"))
                ? scenic.optDouble("longitude") : null;
        return new FeedItem(id, title, description, imageUrl, null,
                scenic.optString("city"),
                TextUtils.isEmpty(address) ? null : address,
                lat, lng, null, null, null);
    }

    private List<FeedItem> parseScenicArray(JSONArray array) {
        List<FeedItem> result = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject scenic = array.optJSONObject(i);
            FeedItem item = buildScenicItem(scenic);
            if (item != null) {
                result.add(item);
            }
        }
        return result;
    }

    private FeedItem buildProductItem(JSONObject product) {
        if (product == null) {
            return null;
        }
        long id = product.optLong("id", 0);
        String title = product.optString("name", "商品");
        String actualType = product.optString("type");
        String description = product.optString("description",
                String.format(Locale.getDefault(), "类型：%s", actualType));
        String imageUrl = product.optString("cover_image");
        String priceLabel = formatPrice(product.optDouble("price", Double.NaN));
        String address = product.optString("hotel_address", "");
        if (TextUtils.isEmpty(address)) {
            address = product.optString("address");
        }
        Integer stock = (product.has("stock") && !product.isNull("stock"))
                ? product.optInt("stock") : null;
        return new FeedItem(id, title, description, imageUrl, priceLabel, actualType,
                TextUtils.isEmpty(address) ? null : address,
                null, null,
                stock, null, null);
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
