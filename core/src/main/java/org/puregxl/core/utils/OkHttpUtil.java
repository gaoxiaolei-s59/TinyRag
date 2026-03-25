package org.puregxl.core.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class OkHttpUtil {
    private static final Gson GSON = new Gson();
    private static final MediaType JSON = MediaType.parse("application/json");

    private final OkHttpClient client;
    private final String apiKey;

    public OkHttpUtil(@Value("${ai.chat.bailian.api-key}") String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public JsonObject get(String url) throws IOException {
        Request request = baseBuilder(url)
                .get()
                .build();
        return execute(request);
    }

    public JsonObject get(String url, Map<String, String> queryParams) throws IOException {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        Request request = baseBuilder(urlBuilder.build().toString())
                .get()
                .build();
        return execute(request);
    }

    public JsonObject post(String url, Object requestBody) throws IOException {
        Request request = baseBuilder(url)
                .post(buildJsonBody(requestBody))
                .build();
        return execute(request);
    }

    public JsonObject put(String url, Object requestBody) throws IOException {
        Request request = baseBuilder(url)
                .put(buildJsonBody(requestBody))
                .build();
        return execute(request);
    }

    public JsonObject patch(String url, Object requestBody) throws IOException {
        Request request = baseBuilder(url)
                .patch(buildJsonBody(requestBody))
                .build();
        return execute(request);
    }

    public JsonObject delete(String url) throws IOException {
        Request request = baseBuilder(url)
                .delete()
                .build();
        return execute(request);
    }

    public JsonObject delete(String url, Object requestBody) throws IOException {
        Request request = baseBuilder(url)
                .delete(buildJsonBody(requestBody))
                .build();
        return execute(request);
    }

    private Request.Builder baseBuilder(String url) {
        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json");
    }

    private RequestBody buildJsonBody(Object requestBody) {
        return RequestBody.create(GSON.toJson(requestBody), JSON);
    }

    private JsonObject execute(Request request) throws IOException {
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException("请求失败，code=" + response.code() + ", body=" + errorBody);
            }

            String responseBody = response.body() != null ? response.body().string() : "{}";
            return GSON.fromJson(responseBody, JsonObject.class);
        }
    }
}
