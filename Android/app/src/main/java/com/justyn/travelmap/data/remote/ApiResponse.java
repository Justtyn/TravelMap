package com.justyn.travelmap.data.remote;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Simple representation of backend responses following the unified structure in API_DOC.md.
 */
public class ApiResponse {
    private final int code;
    private final String message;
    private final Object data;

    public ApiResponse(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }

    public boolean isSuccess() {
        return code == 200;
    }

    public static ApiResponse fromJson(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        int code = jsonObject.optInt("code", -1);
        String message = jsonObject.optString("msg", "未知错误");
        Object data = jsonObject.has("data") ? jsonObject.get("data") : null;
        return new ApiResponse(code, message, data);
    }
}
