package org.puregxl.core.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MilvusConfig {

    @Bean(destroyMethod = "close")
    public MilvusClientV2 milvusClient(@Value("${milvus.uri}") String milvusUri) {
        ConnectConfig connectConfig = ConnectConfig.builder()
                .uri(milvusUri)
                .build();
        return new MilvusClientV2(connectConfig);
    }
}
