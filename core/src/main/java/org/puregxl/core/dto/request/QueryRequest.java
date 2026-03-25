package org.puregxl.core.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueryRequest {

    /**
     * 模型名，例如 qwen-plus、gpt-4o-mini。
     */
    private String model;

    /**
     * OpenAI 兼容聊天消息列表。
     */
    private List<Message> messages;

    /**
     * 是否流式返回。
     */
    private Boolean stream = false;

    /**
     * 最大生成 token 数。
     */
    private Integer maxTokens;

    /**
     * 采样温度。
     */
    private Double temperature;

    /**
     * 默认格式
     * @param model
     * @param userMessage
     * @return
     */
    public static QueryRequest of(String model, String userMessage) {
        return new QueryRequest(
                model,
                List.of(new Message("user", userMessage)),
                false,
                null,
                0.7
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        /**
         * 角色：system、user、assistant。
         */
        private String role;

        /**
         * 消息内容。
         */
        private String content;
    }
}
