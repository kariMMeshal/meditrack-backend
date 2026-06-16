package com.MediTrack.meditrack_backend.config;

import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching   // activates @Cacheable, @CacheEvict across the whole app
public class RedisConfig {

    @Value("${REDIS_HOST:redis}")
    private String host;

    @Value("${REDIS_PORT:6379}")
    private int port;

    @Value("${REDIS_PASSWORD:}")
    private String password;

    @Bean
    public RedisClient redisClient() {
        try {
            String uri = (password != null && !password.isBlank())
                    ? "redis://default:" + password + "@" + host + ":" + port
                    : "redis://" + host + ":" + port;

            return RedisClient.create(uri);
        } catch (Exception e) {
            throw new IllegalStateException("Redis init failed", e);
        }
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        if (!password.isBlank()) {
            config.setPassword(password);
        }
        return new LettuceConnectionFactory(config);
    }

    /**
     * General-purpose template for manual Redis operations.
     * Keys are Strings, values are JSON-serialized objects.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Cache manager with per-cache TTL configuration.
     * Each cache name maps to its own eviction policy.
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // AI responses — cache for 1 hour (LLM answers to identical prompts don't change)
        cacheConfigs.put("ai-responses",  defaultConfig.entryTtl(Duration.ofHours(1)));

        // ML predictions — cache for 30 minutes per device
        cacheConfigs.put("ml-predictions", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Alert deduplication check — 5 minutes
        cacheConfigs.put("alert-dedup",   defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Device status — cache for 15 minutes
        cacheConfigs.put("device-status", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}