package org.puregxl.core.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParseResult {
    /**
     * 解析是否成功
     */
    private boolean success;

    private String errorMessage;

    private String mimeType;

    private String content;

    /**
     * 元数据
     */
    private Map<String, String> metadata;


    /**
     * 文本长度（字符数）
     */
    private int contentLength;


    public static ParseResult success(String mimeType, String content, Map<String, String> metadata) {
        ParseResult result = new ParseResult();
        result.setSuccess(true);
        result.setMetadata(metadata);
        result.setContent(content);
        result.setMimeType(mimeType);
        result.setContentLength(content == null ? 0 : content.length());
        return result;
    }

    public static ParseResult failure(String errorMessage){
        ParseResult result = new ParseResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }


}
