package com.justyn.travelmap.data.remote;

import com.justyn.travelmap.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 使用原生 HttpURLConnection 简单封装 GET/POST/PUT/DELETE。
 */
public class ApiClient {

    // 请求超时时间 10s
    private static final int TIMEOUT_MS = 10000;

    public ApiResponse get(String path) throws IOException, JSONException {
        return get(path, null);
    }

    public ApiResponse get(String path, Map<String, String> queryParams) throws IOException, JSONException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(resolveUrl(path, queryParams)); // 拼接完整 URL
            connection = (HttpURLConnection) url.openConnection(); // 打开连接
            connection.setRequestMethod("GET"); // 设置 GET
            connection.setConnectTimeout(TIMEOUT_MS); // 连接超时
            connection.setReadTimeout(TIMEOUT_MS); // 读取超时
            connection.setRequestProperty("Accept", "application/json"); // 接收 JSON
            connection.setDoInput(true); // 允许输入流

            int responseCode = connection.getResponseCode(); // 获取状态码
            InputStream stream = responseCode >= HttpURLConnection.HTTP_BAD_REQUEST
                    ? connection.getErrorStream() // 4xx/5xx 用错误流
                    : connection.getInputStream(); // 正常流

            if (stream == null) {
                throw new IOException("服务器未返回数据");
            }

            String responseBody = readStream(stream); // 读取响应体
            return ApiResponse.fromJson(responseBody); // 转换为 ApiResponse
        } finally {
            if (connection != null) {
                connection.disconnect(); // 释放连接
            }
        }
    }

    public ApiResponse post(String path, JSONObject payload) throws IOException, JSONException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(resolveUrl(path)); // 拼完整 URL
            connection = (HttpURLConnection) url.openConnection(); // 打开连接
            connection.setRequestMethod("POST"); // POST 请求
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8"); // JSON 请求体
            connection.setDoOutput(true); // 允许输出

            if (payload != null) {
                byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8); // 序列化 JSON
                connection.setRequestProperty("Content-Length", String.valueOf(body.length)); // 设置长度
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(body); // 写入请求体
                }
            }

            int responseCode = connection.getResponseCode(); // 获取状态码
            InputStream stream = responseCode >= HttpURLConnection.HTTP_BAD_REQUEST
                    ? connection.getErrorStream()
                    : connection.getInputStream();

            if (stream == null) {
                throw new IOException("服务器未返回数据");
            }

            String responseBody = readStream(stream); // 读取响应体
            return ApiResponse.fromJson(responseBody); // 转换为 ApiResponse
        } finally {
            if (connection != null) {
                connection.disconnect(); // 释放连接
            }
        }
    }

    public ApiResponse put(String path, JSONObject payload) throws IOException, JSONException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(resolveUrl(path)); // 拼完整 URL
            connection = (HttpURLConnection) url.openConnection(); // 打开连接
            connection.setRequestMethod("PUT"); // PUT 请求
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8"); // JSON 请求体
            connection.setDoOutput(true);

            if (payload != null) {
                byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
                connection.setRequestProperty("Content-Length", String.valueOf(body.length));
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(body);
                }
            }

            int responseCode = connection.getResponseCode();
            InputStream stream = responseCode >= HttpURLConnection.HTTP_BAD_REQUEST
                    ? connection.getErrorStream()
                    : connection.getInputStream();

            if (stream == null) {
                throw new IOException("服务器未返回数据");
            }

            String responseBody = readStream(stream);
            return ApiResponse.fromJson(responseBody);
        } finally {
            if (connection != null) {
                connection.disconnect(); // 释放连接
            }
        }
    }

    public ApiResponse delete(String path, Map<String, String> queryParams) throws IOException, JSONException {
        return delete(path, queryParams, null);
    }

    public ApiResponse delete(String path, JSONObject payload) throws IOException, JSONException {
        return delete(path, null, payload);
    }

    private static String resolveUrl(String path) {
        return resolveUrl(path, null);
    }

    private static String resolveUrl(String path, Map<String, String> queryParams) {
        if (path.startsWith("http")) {
            return appendQuery(path, queryParams); // 绝对地址直接拼参
        }
        if (path.startsWith("/")) {
            return appendQuery(BuildConfig.API_BASE_URL + path, queryParams); // 以 / 开头直接拼到基址后
        }
        return appendQuery(BuildConfig.API_BASE_URL + "/" + path, queryParams); // 其他情况补一个 /
    }

    private static String appendQuery(String baseUrl, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return baseUrl; // 没有查询参数直接返回
        }
        StringJoiner joiner = new StringJoiner("&"); // 用于拼接 key=value
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue; // 跳过空值
            }
            joiner.add(encode(entry.getKey()) + "=" + encode(entry.getValue())); // 追加编码后的键值对
        }
        String queryString = joiner.toString(); // 完整查询字符串
        if (queryString.isEmpty()) {
            return baseUrl;
        }
        return baseUrl.contains("?") ? baseUrl + "&" + queryString : baseUrl + "?" + queryString; // 根据是否已有 ? 选择拼接方式
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name()); // UTF-8 URL 编码
        } catch (Exception e) {
            return value; // 异常时返回原值
        }
    }

    private static String readStream(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder(); // 用于累积每行
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line); // 持续追加
            }
        }
        return builder.toString(); // 返回完整字符串
    }

    private ApiResponse delete(String path, Map<String, String> queryParams, JSONObject payload) throws IOException, JSONException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(resolveUrl(path, queryParams)); // 拼 URL
            connection = (HttpURLConnection) url.openConnection(); // 打开连接
            connection.setRequestMethod("DELETE"); // DELETE 请求
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            if (payload != null) {
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8"); // JSON 请求体
                connection.setDoOutput(true);
                byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
                connection.setRequestProperty("Content-Length", String.valueOf(body.length));
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(body); // 写入请求体
                }
            }

            int responseCode = connection.getResponseCode(); // 获取状态码
            InputStream stream = responseCode >= HttpURLConnection.HTTP_BAD_REQUEST
                    ? connection.getErrorStream()
                    : connection.getInputStream();

            if (stream == null) {
                throw new IOException("服务器未返回数据");
            }

            String responseBody = readStream(stream); // 读取响应
            return ApiResponse.fromJson(responseBody); // 转换响应
        } finally {
            if (connection != null) {
                connection.disconnect(); // 释放连接
            }
        }
    }
}
