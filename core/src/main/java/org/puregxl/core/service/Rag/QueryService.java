package org.puregxl.core.service.Rag;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.puregxl.core.dto.request.QueryRequest;
import org.puregxl.core.utils.OkHttpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class QueryService {

    private final OkHttpUtil okHttpUtil;

    @Value("${ai.chat.bailian.url}")
    private String apiUrl;

    @Value("${ai.chat.bailian.model}")
    private String model;


    /**
     * 执行用户的请求
     * @param message
     */
    public String queryV1(String message) throws IOException {
        QueryRequest queryRequest = QueryRequest.of(model, message);
        JsonObject result = okHttpUtil.post(apiUrl, queryRequest);
        return extractContent(result);
    }


    /**
     * 解析内容
     * @param result
     * @return
     */
    private String extractContent(JsonObject result) {
        JsonArray choices = result.getAsJsonArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("响应中没有 choices: " + result);
        }

        JsonObject firstChoice = choices.get(0).getAsJsonObject();
        JsonObject messageObject = firstChoice.getAsJsonObject("message");
        if (messageObject == null || !messageObject.has("content")) {
            throw new IllegalStateException("响应中没有 message.content: " + result);
        }

        return messageObject.get("content").getAsString();
    }
}
