package com.heima.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.*;

import java.time.Duration;

/**
 * AI 模块专用 Redis 配置。
 * 类名不能与 com.heima.common.redis.RedisConfig 重名，否则两个应用的组件扫描会冲突。
 */
@Configuration
public class AiRedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private int redisPort;
    @Value("${spring.data.redis.password:}") // 如果 Redis 有密码可以配上，没有则为空
    private String redisPassword;

    @Bean
    public JedisPooled jedisPooled() {
        // 1. 配置连接池参数
        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();

        // 【关键配置】开启空闲连接定时检测
        poolConfig.setTestWhileIdle(true);
        // 每 30 秒后台检测一次连接池中的空闲连接
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
        // 发现死连接直接清理
        poolConfig.setMinEvictableIdleDuration(Duration.ofSeconds(60));

        // 连接池容量配置（根据业务情况调整）
        poolConfig.setMaxTotal(30);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(2);

        // 2. 配置客户端超时参数（防止网络抖动导致的阻塞）
        DefaultJedisClientConfig.Builder clientConfigBuilder = DefaultJedisClientConfig.builder()
                .connectionTimeoutMillis(3000) // 建立连接超时 3 秒
                .socketTimeoutMillis(3000);    // 读写数据超时 3 秒

        if (redisPassword != null && !redisPassword.isEmpty()) {
            clientConfigBuilder.password(redisPassword);
        }

        JedisClientConfig clientConfig = clientConfigBuilder.build();
        HostAndPort hostAndPort = new HostAndPort(redisHost, redisPort);

        // 3. 构建包含防御性连接池配置的 JedisPooled 实例
        return new JedisPooled(hostAndPort, clientConfig, poolConfig);
    }
}
