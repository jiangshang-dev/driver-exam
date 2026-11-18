package com.heima.ai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vector-store.redis")
@Data
public class RedisVectorStoreProperties {
    private String host;
    private int port;
    private String password;
    private int database;
    private String indexName;
    private String prefix;
}