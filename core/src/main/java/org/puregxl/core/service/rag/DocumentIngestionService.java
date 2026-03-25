package org.puregxl.core.service.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.puregxl.core.dto.ParseResult;
import org.puregxl.core.service.TikaParseService;
import org.puregxl.core.service.embedding.EmbeddingService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final TikaParseService tikaParseService;
    private final EmbeddingService embeddingService;
    private final MilvusVectorStoreService milvusVectorStoreService;

    public ParseResult parseAndStore(MultipartFile file) throws IOException {
        ParseResult parseResult = tikaParseService.parseFile(file);
        if (!parseResult.isSuccess()) {
            return parseResult;
        }

        List<String> chunks = parseResult.getChunks();
        if (chunks == null || chunks.isEmpty()) {
            return ParseResult.failure("文档切块结果为空，无法写入向量库");
        }

        String docId = UUID.randomUUID().toString().replace("-", "");
        String sourceFile = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();

        milvusVectorStoreService.ensureCollectionReady();
        List<List<Float>> vectors = embeddingService.embed(chunks);
        long insertedCount = milvusVectorStoreService.insertChunks(docId, sourceFile, chunks, vectors);

        log.info("文档 {} 已写入向量库，docId={}, insertedCount={}", sourceFile, docId, insertedCount);
        return parseResult.withStorage(docId, milvusVectorStoreService.getCollectionName(), insertedCount);
    }
}
