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
import java.nio.charset.StandardCharsets;

/**
 * Minimal HTTP client for communicating with the TravelMap backend using HttpURLConnection.
 */
public class ApiClient {

    private static final int TIMEOUT_MS = 10000;

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
        if (path.startsWith("http")) {
            return path;
        }
        if (path.startsWith("/")) {
            return BuildConfig.API_BASE_URL + path;
        }
        return BuildConfig.API_BASE_URL + "/" + path;
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
