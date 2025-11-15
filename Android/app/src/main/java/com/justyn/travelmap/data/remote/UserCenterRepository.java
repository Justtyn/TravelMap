package com.justyn.travelmap.data.remote;

import android.text.TextUtils;

import com.justyn.travelmap.model.CartItem;
import com.justyn.travelmap.model.FeedItem;
import com.justyn.travelmap.model.OrderDetail;
import com.justyn.travelmap.model.OrderItemDetail;
import com.justyn.travelmap.model.VisitedRecord;

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
                    order.optString("create_time"), null, null, null, null, null, null));
        }
        return items;
    }

    public OrderDetail fetchOrderDetail(long orderId) throws IOException, JSONException {
        ApiResponse response = apiClient.get("/api/orders/" + orderId);
        ensureSuccess(response);
        Object data = response.getData();
        if (data instanceof JSONObject) {
            return buildOrderDetail((JSONObject) data);
        }
        return null;
    }

    public List<CartItem> fetchCart(long userId) throws IOException, JSONException {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(userId));
        ApiResponse response = apiClient.get("/api/cart", params);
        ensureSuccess(response);
        Object data = response.getData();
        List<CartItem> result = new ArrayList<>();
        if (!(data instanceof JSONArray)) {
            return result;
        }
        JSONArray array = (JSONArray) data;
        for (int i = 0; i < array.length(); i++) {
            JSONObject cartJson = array.optJSONObject(i);
            if (cartJson == null) {
                continue;
            }
            JSONObject productJson = cartJson.optJSONObject("product");
            FeedItem product = productJson == null ? null : buildProductItem(productJson);
            double unitPrice = productJson != null ? productJson.optDouble("price", Double.NaN) : Double.NaN;
            result.add(new CartItem(
                    cartJson.optLong("cart_id", i),
                    cartJson.optInt("quantity", 1),
                    product,
                    unitPrice
            ));
        }
        return result;
    }

    public List<FeedItem> fetchVisited(long userId) throws IOException, JSONException {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(userId));
        ApiResponse response = apiClient.get("/api/visited", params);
        ensureSuccess(response);
        Object data = response.getData();
        List<FeedItem> result = new ArrayList<>();
        if (!(data instanceof JSONArray)) {
            return result;
        }
        JSONArray array = (JSONArray) data;
        for (int i = 0; i < array.length(); i++) {
            JSONObject visited = array.optJSONObject(i);
            if (visited == null) {
                continue;
            }
            JSONObject scenic = visited.optJSONObject("scenic");
            if (scenic == null) {
                continue;
            }
            long scenicId = scenic.optLong("id", i);
            String title = scenic.optString("name", "景点");
            String description = scenic.optString("description", scenic.optString("city", ""));
            String imageUrl = scenic.optString("cover_image");
            String address = scenic.optString("address");
            Double lat = (scenic.has("latitude") && !scenic.isNull("latitude"))
                    ? scenic.optDouble("latitude") : null;
            Double lng = (scenic.has("longitude") && !scenic.isNull("longitude"))
                    ? scenic.optDouble("longitude") : null;
            String visitTime = visited.optString("visit_date");
            int ratingValue = visited.optInt("rating", -1);
            String ratingLabel = ratingValue >= 0
                    ? String.format(Locale.getDefault(), "评分：%d/5", ratingValue)
                    : null;
            result.add(new FeedItem(scenicId, title, description, imageUrl, null,
                    scenic.optString("city"), address, lat, lng, null, visitTime, ratingLabel));
        }
        return result;
    }

    public JSONObject createOrder(long userId,
                                  String contactName,
                                  String contactPhone,
                                  String orderType,
                                  String checkinDate,
                                  String checkoutDate) throws IOException, JSONException {
        JSONObject payload = new JSONObject();
        payload.put("user_id", userId);
        if (!TextUtils.isEmpty(contactName)) {
            payload.put("contact_name", contactName);
        }
        if (!TextUtils.isEmpty(contactPhone)) {
            payload.put("contact_phone", contactPhone);
        }
        if (!TextUtils.isEmpty(orderType)) {
            payload.put("order_type", orderType);
        }
        if (!TextUtils.isEmpty(checkinDate)) {
            payload.put("checkin_date", checkinDate);
        }
        if (!TextUtils.isEmpty(checkoutDate)) {
            payload.put("checkout_date", checkoutDate);
        }
        ApiResponse response = apiClient.post("/api/orders", payload);
        ensureSuccess(response);
        Object data = response.getData();
        return data instanceof JSONObject ? (JSONObject) data : null;
    }

    public void addToCart(long userId, long productId, int quantity) throws IOException, JSONException {
        JSONObject payload = new JSONObject();
        payload.put("user_id", userId);
        payload.put("product_id", productId);
        payload.put("quantity", quantity);
        ApiResponse response = apiClient.post("/api/cart", payload);
        ensureSuccess(response);
    }

    public JSONObject updateCartItem(long cartId, int quantity) throws IOException, JSONException {
        JSONObject payload = new JSONObject();
        payload.put("quantity", quantity);
        ApiResponse response = apiClient.put("/api/cart/" + cartId, payload);
        ensureSuccess(response);
        Object data = response.getData();
        return data instanceof JSONObject ? (JSONObject) data : null;
    }

    public void deleteCartItem(long cartId) throws IOException, JSONException {
        ApiResponse response = apiClient.delete("/api/cart/" + cartId, (JSONObject) null);
        ensureSuccess(response);
    }

    public boolean isFavorite(long userId, long targetId, String targetType) throws IOException, JSONException {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(userId));
        params.put("target_id", String.valueOf(targetId));
        params.put("target_type", targetType);
        ApiResponse response = apiClient.get("/api/favorites/status", params);
        ensureSuccess(response);
        Object data = response.getData();
        if (data instanceof JSONObject) {
            JSONObject json = (JSONObject) data;
            return json.optBoolean("favorited", false);
        }
        return false;
    }

    public void addFavorite(long userId, long targetId, String targetType) throws IOException, JSONException {
        JSONObject payload = new JSONObject();
        payload.put("user_id", userId);
        payload.put("target_id", targetId);
        payload.put("target_type", targetType);
        ApiResponse response = apiClient.post("/api/favorites", payload);
        ensureSuccess(response);
    }

    public void removeFavorite(long userId, long targetId, String targetType) throws IOException, JSONException {
        JSONObject payload = new JSONObject();
        payload.put("user_id", userId);
        payload.put("target_id", targetId);
        payload.put("target_type", targetType);
        ApiResponse response = apiClient.delete("/api/favorites", payload);
        ensureSuccess(response);
    }

    public void addVisited(long userId, long scenicId, int rating) throws IOException, JSONException {
        JSONObject payload = new JSONObject();
        payload.put("user_id", userId);
        payload.put("scenic_id", scenicId);
        payload.put("rating", rating);
        ApiResponse response = apiClient.post("/api/visited", payload);
        ensureSuccess(response);
    }

    public void removeVisited(long userId, long scenicId) throws IOException, JSONException {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(userId));
        params.put("scenic_id", String.valueOf(scenicId));
        ApiResponse response = apiClient.delete("/api/visited", params);
        ensureSuccess(response);
    }

    public VisitedRecord getVisitedRecord(long userId, long scenicId) throws IOException, JSONException {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(userId));
        ApiResponse response = apiClient.get("/api/visited", params);
        ensureSuccess(response);
        Object data = response.getData();
        if (!(data instanceof JSONArray)) {
            return null;
        }
        JSONArray array = (JSONArray) data;
        for (int i = 0; i < array.length(); i++) {
            JSONObject visited = array.optJSONObject(i);
            if (visited == null) continue;
            if (visited.optLong("scenic_id") == scenicId) {
                return new VisitedRecord(
                        visited.optLong("visited_id"),
                        visited.optInt("rating", -1),
                        visited.optString("visit_date")
                );
            }
        }
        return null;
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
                    lat, lng, stock, null, null));
        }
        return items;
    }

    private FeedItem buildProductItem(JSONObject productJson) {
        if (productJson == null) {
            return null;
        }
        long id = productJson.optLong("id", 0);
        String title = productJson.optString("name", "商品");
        String description = productJson.optString("description", "");
        String imageUrl = productJson.optString("cover_image");
        String priceLabel = formatPrice(productJson.optDouble("price", Double.NaN));
        String address = productJson.optString("hotel_address", null);
        if (TextUtils.isEmpty(address)) {
            address = productJson.optString("address");
        }
        Double lat = (productJson.has("latitude") && !productJson.isNull("latitude"))
                ? productJson.optDouble("latitude") : null;
        Double lng = (productJson.has("longitude") && !productJson.isNull("longitude"))
                ? productJson.optDouble("longitude") : null;
        Integer stock = (productJson.has("stock") && !productJson.isNull("stock"))
                ? productJson.optInt("stock") : null;
        return new FeedItem(id, title, description, imageUrl, priceLabel,
                productJson.optString("type"), TextUtils.isEmpty(address) ? null : address,
                lat, lng, stock, null, null);
    }

    private OrderDetail buildOrderDetail(JSONObject orderJson) {
        if (orderJson == null) {
            return null;
        }
        long orderId = orderJson.optLong("id", 0);
        String orderNo = orderJson.optString("order_no");
        String status = orderJson.optString("status");
        String orderType = orderJson.optString("order_type");
        double totalPrice = orderJson.optDouble("total_price", Double.NaN);
        String contactName = orderJson.optString("contact_name");
        String contactPhone = orderJson.optString("contact_phone");
        String createTime = orderJson.optString("create_time");
        String checkinDate = orderJson.optString("checkin_date");
        String checkoutDate = orderJson.optString("checkout_date");
        List<OrderItemDetail> itemDetails = new ArrayList<>();
        JSONArray items = orderJson.optJSONArray("items");
        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                JSONObject itemJson = items.optJSONObject(i);
                if (itemJson == null) {
                    continue;
                }
                JSONObject productJson = itemJson.optJSONObject("product");
                FeedItem product = productJson == null ? null : buildProductItem(productJson);
                itemDetails.add(new OrderItemDetail(
                        itemJson.optLong("order_item_id", itemJson.optLong("id", i)),
                        itemJson.optInt("quantity", 1),
                        itemJson.optDouble("price", Double.NaN),
                        product
                ));
            }
        }
        return new OrderDetail(orderId, orderNo, status, orderType, totalPrice,
                contactName, contactPhone, createTime, checkinDate, checkoutDate, itemDetails);
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
