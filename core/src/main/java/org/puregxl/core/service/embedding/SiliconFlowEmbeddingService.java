package org.puregxl.core.service.embedding;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SiliconFlowEmbeddingService implements EmbeddingService {

    private static final String DEFAULT_API_URL = "https://api.siliconflow.cn/v1/embeddings";
    private static final String DEFAULT_MODEL = "Qwen/Qwen3-Embedding-8B";
    private static final Gson GSON = new Gson();
    private static final MediaType JSON = MediaType.parse("application/json");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    @Value("${ai.embedding.siliconflow.url:https://api.siliconflow.cn/v1/embeddings}")
    private String embeddingUrl;

    @Value("${ai.embedding.siliconflow.api-key}")
    private String apiKey;

    @Value("${ai.embedding.siliconflow.model:Qwen/Qwen3-Embedding-8B}")
    private String model;


    @Override
    public List<List<Float>> embed(List<String> texts) throws IOException {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        String requestUrl = (embeddingUrl == null || embeddingUrl.isBlank()) ? DEFAULT_API_URL : embeddingUrl;
        String requestModel = (model == null || model.isBlank()) ? DEFAULT_MODEL : model;

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", requestModel);
        requestBody.add("input", GSON.toJsonTree(texts));
        requestBody.addProperty("encoding_format", "float");

        Request request = new Request.Builder()
                .url(requestUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(GSON.toJson(requestBody), JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "";
                throw new IOException("Embedding 调用失败，code=" + response.code() + ", body=" + errorBody);
            }

            String body = response.body() != null ? response.body().string() : "{}";
            JsonObject json = GSON.fromJson(body, JsonObject.class);
            JsonArray dataArray = json.getAsJsonArray("data");

            List<List<Float>> vectors = new ArrayList<>();
            for (int i = 0; i < dataArray.size(); i++) {
                JsonArray embeddingArray = dataArray.get(i).getAsJsonObject().getAsJsonArray("embedding");
                List<Float> vector = new ArrayList<>(embeddingArray.size());
                for (int j = 0; j < embeddingArray.size(); j++) {
                    vector.add(embeddingArray.get(j).getAsFloat());
                }
                vectors.add(vector);
            }
            return vectors;
        }
    }
}
