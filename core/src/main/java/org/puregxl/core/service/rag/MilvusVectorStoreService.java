package org.puregxl.core.service.rag;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.GetLoadStateReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.response.InsertResp;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MilvusVectorStoreService {

    private static final Gson GSON = new Gson();

    private final MilvusClientV2 milvusClient;

    @Value("${rag.collection-name}")
    private String collectionName;

    @Value("${rag.dimension}")
    private Integer dimension;

    @Value("${rag.metric-type}")
    private String metricType;

    public String getCollectionName() {
        return collectionName;
    }

    public void ensureCollectionReady() {
        Boolean exists = milvusClient.hasCollection(HasCollectionReq.builder()
                .collectionName(collectionName)
                .build());
        if (Boolean.FALSE.equals(exists)) {
            createCollection();
        }

        Boolean loaded = milvusClient.getLoadState(GetLoadStateReq.builder()
                .collectionName(collectionName)
                .build());
        if (Boolean.FALSE.equals(loaded)) {
            milvusClient.loadCollection(LoadCollectionReq.builder()
                    .collectionName(collectionName)
                    .build());
        }
    }

    public long insertChunks(String docId, String sourceFile, List<String> chunks, List<List<Float>> vectors) {
        if (chunks.size() != vectors.size()) {
            throw new IllegalArgumentException("切块数与向量数不一致");
        }

        List<JsonObject> rows = new ArrayList<>(chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            JsonObject row = new JsonObject();
            row.addProperty("doc_id", docId);
            row.addProperty("source_file", sourceFile);
            row.addProperty("chunk_index", i);
            row.addProperty("chunk_text", chunks.get(i));
            row.add("vector", GSON.toJsonTree(vectors.get(i)));
            rows.add(row);
        }

        InsertResp insertResp = milvusClient.insert(InsertReq.builder()
                .collectionName(collectionName)
                .data(rows)
                .build());
        return insertResp.getInsertCnt();
    }

    private void createCollection() {
        CreateCollectionReq.CollectionSchema schema = milvusClient.createSchema();
        schema.addField(AddFieldReq.builder()
                .fieldName("id")
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .autoID(true)
                .build());
        schema.addField(AddFieldReq.builder()
                .fieldName("doc_id")
                .dataType(DataType.VarChar)
                .maxLength(128)
                .build());
        schema.addField(AddFieldReq.builder()
                .fieldName("source_file")
                .dataType(DataType.VarChar)
                .maxLength(512)
                .build());
        schema.addField(AddFieldReq.builder()
                .fieldName("chunk_index")
                .dataType(DataType.Int64)
                .build());
        schema.addField(AddFieldReq.builder()
                .fieldName("chunk_text")
                .dataType(DataType.VarChar)
                .maxLength(8192)
                .build());
        schema.addField(AddFieldReq.builder()
                .fieldName("vector")
                .dataType(DataType.FloatVector)
                .dimension(dimension)
                .build());

        milvusClient.createCollection(CreateCollectionReq.builder()
                .collectionName(collectionName)
                .collectionSchema(schema)
                .build());

        IndexParam vectorIndex = IndexParam.builder()
                .fieldName("vector")
                .indexType(IndexParam.IndexType.HNSW)
                .metricType(IndexParam.MetricType.valueOf(metricType))
                .extraParams(Map.of(
                        "M", 16,
                        "efConstruction", 256
                ))
                .build();

        IndexParam docIdIndex = IndexParam.builder()
                .fieldName("doc_id")
                .indexType(IndexParam.IndexType.TRIE)
                .build();

        milvusClient.createIndex(CreateIndexReq.builder()
                .collectionName(collectionName)
                .indexParams(List.of(vectorIndex, docIdIndex))
                .build());
    }
}
