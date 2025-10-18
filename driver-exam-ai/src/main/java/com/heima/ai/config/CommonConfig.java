package com.heima.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.heima.ai.utils.RedisChatMemoryStore;

import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.Resource;

@Configuration
public class CommonConfig {

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    // 构建会议记忆对象
    @Bean
    public ChatMemory chatMemory(){
        return MessageWindowChatMemory.builder()
                .maxMessages(10) // 配置会话记忆对象的最大消息数
                .build();
    }

    // 构建会议记忆ChatMemoryProvider对象提供器
    @Bean
    public ChatMemoryProvider chatMemoryProvider(){
        // 注意：这里不能在Bean初始化时获取用户信息，因为此时可能没有Web请求上下文
        // 用户信息应该在实际调用时通过memoryId参数传递
        return memoryId -> MessageWindowChatMemory.builder() // 构建会话记忆对象构建器
                .id(memoryId) // 配置会话记忆对象的唯一标识
                .maxMessages(20) // 配置会话记忆对象的最大消息数
                .chatMemoryStore(redisChatMemoryStore)
                .build(); // 构建会话记忆对象
    }

    // 构建向量数据库操作对象
    @Bean
    public EmbeddingStore store(){
        // 1.加载文档进内容
        // List<Document> documents = ClassPathDocumentLoader.loadDocuments("content", new ApachePdfBoxDocumentParser());
        // 2.构建向量数据库操作对象
        InMemoryEmbeddingStore store = new InMemoryEmbeddingStore();

        // 构建文档分割器  最大容纳的字符数量500，两个片段之间最多的重复字符100
        DocumentSplitter ds = DocumentSplitters.recursive(500, 100);

        // 3.构建一个EmbeddingStoreIngestor对象，完成文本数据切割，存储
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(store) // 配置向量数据库操作对象
                .documentSplitter(ds) // 配置文档分割器
                .build();
        // 4.使用ingestor对象完成文本数据的切割和存储
       // ingestor.ingest(documents);
        return store;
    }

    // 构建向量数据库检索对象
    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore store){
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .minScore(0.5) // 配置cos相似度阈值分数
                .maxResults(3) // 配置最大检索结果数
                .build();
    }

}