package com.xushu.rag.config;


import io.milvus.client.MilvusServiceClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class VectorStoreConfig {

//    // 从yml读取记忆集合名
//    @Value("${milvus.vector-store.memory-collection:local_memory}")
//    private String memoryCollectionName;
//
////    // 从yml读取文档集合名
//    @Value("${milvus.vector-store.document-collection:document_memory}")
//    private String documentCollectionName;
//
//    @Bean("memoryVectorStore")
//    public VectorStore memoryVectorStore(MilvusServiceClient milvusServiceClient, EmbeddingModel embeddingModel) {
//        return MilvusVectorStore.builder(milvusServiceClient, embeddingModel)
//                .collectionName(memoryCollectionName).build();
//    }
//
//    @Bean("documentVectorStore")
//    public VectorStore documentVectorStore(MilvusServiceClient milvusServiceClient, EmbeddingModel embeddingModel) {
//        return MilvusVectorStore.builder(milvusServiceClient, embeddingModel)
//                .collectionName(documentCollectionName).build();
//
//    }
}
