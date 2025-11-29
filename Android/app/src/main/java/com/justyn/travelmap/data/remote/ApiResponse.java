package com.justyn.travelmap.data.remote;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 后端统一返回结构的简单封装。
 */
public class ApiResponse {
    // 业务状态码
    private final int code;
    // 业务消息
    private final String message;
    // 具体数据，可为任意类型
    private final Object data;

    public ApiResponse(int code, String message, Object data) {
        this.code = code; // 业务状态码
        this.message = message; // 提示信息
        this.data = data; // 数据载荷
    }

    public int getCode() {
        return code; // 返回状态码
    }

    public String getMessage() {
        return message; // 返回提示信息
    }

    public Object getData() {
        return data; // 返回数据
    }

    public boolean isSuccess() {
        return code == 200; // 约定 200 表示成功
    }

    public static ApiResponse fromJson(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString); // 解析字符串为 JSON 对象
        int code = jsonObject.optInt("code", -1); // 读取 code，默认 -1
        String message = jsonObject.optString("msg", "未知错误"); // 读取 msg，默认未知错误
        Object data = jsonObject.has("data") ? jsonObject.get("data") : null; // 有 data 则取出
        return new ApiResponse(code, message, data); // 构造响应对象
    }
}
