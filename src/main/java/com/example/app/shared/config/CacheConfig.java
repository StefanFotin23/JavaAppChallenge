package com.example.app.shared.config;

import com.example.app.starwars.dto.CharacterResponseDTO;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

@EnableCaching
@Configuration
public class CacheConfig {
  @Value("${spring.cache.redis.time-to-live-minutes:10}")
  private long ttlMinutes;

  @Bean
  public RedisCacheConfiguration cacheConfiguration(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
    com.fasterxml.jackson.databind.ObjectMapper mapper = objectMapper.copy();
    mapper.activateDefaultTyping(
        mapper.getPolymorphicTypeValidator(),
        com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL,
        com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY);

    org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer serializer = 
        new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer(mapper);

    return RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(ttlMinutes))
        .disableCachingNullValues()
        .serializeKeysWith(
            org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer(new org.springframework.data.redis.serializer.StringRedisSerializer()))
        .serializeValuesWith(
            org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer(serializer));
  }

  @Bean
  public RedisTemplate<String, CharacterResponseDTO> characterRedisTemplate(
      RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, CharacterResponseDTO> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    RedisSerializer<Object> serializer = RedisSerializer.json();

    template.setKeySerializer(RedisSerializer.string());
    template.setValueSerializer(serializer);
    template.setHashKeySerializer(RedisSerializer.string());
    template.setHashValueSerializer(serializer);

    return template;
  }

  @Bean
  public CacheManager cacheManager(RedisConnectionFactory connectionFactory, com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
    return org.springframework.data.redis.cache.RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(cacheConfiguration(objectMapper))
        .build();
  }
}
