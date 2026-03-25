package org.puregxl.core.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
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

    /**
     * 切块后的文本片段
     */
    private List<String> chunks;

    /**
     * 切块数量
     */
    private int chunkCount;

    /**
     * 归档入向量库的文档标识
     */
    private String docId;

    /**
     * 向量库集合名
     */
    private String collectionName;

    /**
     * 成功写入的向量条数
     */
    private long insertedCount;


    public static ParseResult success(String mimeType, String content, Map<String, String> metadata, List<String> chunks) {
        ParseResult result = new ParseResult();
        result.setSuccess(true);
        result.setMetadata(metadata);
        result.setContent(content);
        result.setMimeType(mimeType);
        result.setContentLength(content == null ? 0 : content.length());
        result.setChunks(chunks);
        result.setChunkCount(chunks == null ? 0 : chunks.size());
        return result;
    }

    public static ParseResult failure(String errorMessage){
        ParseResult result = new ParseResult();
        result.setSuccess(false);
        result.setErrorMessage(errorMessage);
        return result;
    }

    public ParseResult withStorage(String docId, String collectionName, long insertedCount) {
        this.docId = docId;
        this.collectionName = collectionName;
        this.insertedCount = insertedCount;
        return this;
    }


}
