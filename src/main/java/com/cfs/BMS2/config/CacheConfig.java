package com.cfs.BMS2.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class CacheConfig {

    @Value("${spring.cache.redis.time-to-live:300000}")
    private long cacheTtlMillis;

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        GenericJacksonJsonRedisSerializer genericJacksonJsonRedisSerializer =
                GenericJacksonJsonRedisSerializer.builder().build();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(cacheTtlMillis))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.
                        fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.
                                fromSerializer(genericJacksonJsonRedisSerializer)
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}