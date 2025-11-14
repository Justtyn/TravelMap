package com.justyn.travelmap.data.remote;

import android.text.TextUtils;

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
 * 用户中心相关 API 调用：更新资料、收藏、订单等。
 */
public class UserCenterRepository {

    private final ApiClient apiClient;

    public UserCenterRepository() {
        this(new ApiClient());
    }

    public UserCenterRepository(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public JSONObject updateUserContact(long userId, String phone, String email) throws IOException, JSONException {
        JSONObject payload = new JSONObject();
        payload.put("phone", phone);
        payload.put("email", email);
        ApiResponse response = apiClient.put("/api/users/" + userId, payload);
        ensureSuccess(response);
        Object data = response.getData();
        if (data instanceof JSONObject) {
            return (JSONObject) data;
        }
        return null;
    }

    public List<FeedItem> fetchFavoriteProducts(long userId) throws IOException, JSONException {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(userId));
        ApiResponse response = apiClient.get("/api/favorites/products", params);
        ensureSuccess(response);
        return parseFavoriteList(response.getData(), true);
    }

    public List<FeedItem> fetchFavoriteScenics(long userId) throws IOException, JSONException {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(userId));
        ApiResponse response = apiClient.get("/api/favorites/scenics", params);
        ensureSuccess(response);
        return parseFavoriteList(response.getData(), false);
    }

    public List<FeedItem> fetchOrders(long userId) throws IOException, JSONException {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(userId));
        ApiResponse response = apiClient.get("/api/orders", params);
        ensureSuccess(response);
        Object data = response.getData();
        List<FeedItem> items = new ArrayList<>();
        if (!(data instanceof JSONArray)) {
            return items;
        }
        JSONArray orders = (JSONArray) data;
        for (int i = 0; i < orders.length(); i++) {
            JSONObject order = orders.optJSONObject(i);
            if (order == null) {
                continue;
            }
            long orderId = order.optLong("id", i);
            JSONArray orderItems = order.optJSONArray("items");
            JSONObject firstItem = orderItems != null && orderItems.length() > 0 ? orderItems.optJSONObject(0) : null;
            JSONObject product = firstItem != null ? firstItem.optJSONObject("product") : null;
            String title = order.optString("order_no", "订单");
            if (product != null) {
                title = product.optString("name", title);
            }
            String status = order.optString("status", "");
            String description = String.format(Locale.getDefault(),
                    "%s · %s", order.optString("order_type", "ORDER"), status);
            String imageUrl = product != null ? product.optString("cover_image") : null;
            double totalPrice = order.optDouble("total_price", Double.NaN);
            String priceLabel = Double.isNaN(totalPrice) ? null : String.format(Locale.getDefault(), "¥%.2f", totalPrice);
            items.add(new FeedItem(orderId, title, description, imageUrl, priceLabel,
                    order.optString("create_time"), null, null, null, null));
        }
        return items;
    }

    private List<FeedItem> parseFavoriteList(Object data, boolean isProduct) {
        List<FeedItem> items = new ArrayList<>();
        if (!(data instanceof JSONArray)) {
            return items;
        }
        JSONArray array = (JSONArray) data;
        for (int i = 0; i < array.length(); i++) {
            JSONObject favorite = array.optJSONObject(i);
            if (favorite == null) {
                continue;
            }
            JSONObject target = favorite.optJSONObject("target");
            if (target == null) {
                continue;
            }
            long id = target.optLong("id", i);
            String title = target.optString("name", "收藏项");
            String description = isProduct
                    ? target.optString("description", "旅行好物")
                    : target.optString("description", "热门景点");
            String imageUrl = target.optString("cover_image");
            String extra = isProduct ? target.optString("type") : target.optString("city");
            String priceLabel = isProduct ? formatPrice(target.optDouble("price", Double.NaN)) : null;
            String address = target.optString("address", null);
            if (TextUtils.isEmpty(address) && isProduct) {
                address = target.optString("hotel_address", null);
            }
            Double lat = (target.has("latitude") && !target.isNull("latitude"))
                    ? target.optDouble("latitude") : null;
            Double lng = (target.has("longitude") && !target.isNull("longitude"))
                    ? target.optDouble("longitude") : null;
            Integer stock = (target.has("stock") && !target.isNull("stock"))
                    ? target.optInt("stock") : null;
            items.add(new FeedItem(id, title, description, imageUrl, priceLabel, extra,
                    TextUtils.isEmpty(address) ? null : address,
                    lat, lng, stock));
        }
        return items;
    }

    private String formatPrice(double price) {
        if (Double.isNaN(price)) {
            return null;
        }
        return String.format(Locale.getDefault(), "¥%s",
                price % 1 == 0 ? String.format(Locale.getDefault(), "%.0f", price)
                        : String.format(Locale.getDefault(), "%.2f", price));
    }

    private void ensureSuccess(ApiResponse response) throws IOException {
        if (response == null || !response.isSuccess()) {
            String message = response != null ? response.getMessage() : "未知错误";
            throw new IOException("接口调用失败：" + message);
        }
    }
}
