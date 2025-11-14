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
 * Minimal HTTP client for communicating with the TravelMap backend using HttpURLConnection.
 */
public class ApiClient {

    private static final int TIMEOUT_MS = 10000;

    public ApiResponse get(String path) throws IOException, JSONException {
        return get(path, null);
    }

    public ApiResponse get(String path, Map<String, String> queryParams) throws IOException, JSONException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(resolveUrl(path, queryParams));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoInput(true);

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
                connection.disconnect();
            }
        }
    }

    public ApiResponse post(String path, JSONObject payload) throws IOException, JSONException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(resolveUrl(path));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
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
                connection.disconnect();
            }
        }
    }

    private static String resolveUrl(String path) {
        return resolveUrl(path, null);
    }

    private static String resolveUrl(String path, Map<String, String> queryParams) {
        if (path.startsWith("http")) {
            return appendQuery(path, queryParams);
        }
        if (path.startsWith("/")) {
            return appendQuery(BuildConfig.API_BASE_URL + path, queryParams);
        }
        return appendQuery(BuildConfig.API_BASE_URL + "/" + path, queryParams);
    }

    private static String appendQuery(String baseUrl, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return baseUrl;
        }
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }
            joiner.add(encode(entry.getKey()) + "=" + encode(entry.getValue()));
        }
        String queryString = joiner.toString();
        if (queryString.isEmpty()) {
            return baseUrl;
        }
        return baseUrl.contains("?") ? baseUrl + "&" + queryString : baseUrl + "?" + queryString;
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return value;
        }
    }

    private static String readStream(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }
}
