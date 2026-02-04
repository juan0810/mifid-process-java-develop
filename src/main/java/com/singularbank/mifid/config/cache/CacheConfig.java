package com.singularbank.mifid.config.cache;

import static com.singularbank.mifid.utils.CacheNames.CONVENIENCE_QUESTIONS_CACHE;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "cache.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class CacheConfig {

  @Value("${cache.initialCapacity}")
  private int initialCapacity;

  @Value("${cache.maximumSize}")
  private long maximumSize;

  @Value("${cache.duration.ofMinutes}")
  private long durationOfMinutes;

  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager(CONVENIENCE_QUESTIONS_CACHE);
    cacheManager.setCaffeine(caffeineCacheBuilder());
    return cacheManager;
  }

  private Caffeine<Object, Object> caffeineCacheBuilder() {
    return Caffeine.newBuilder()
        .initialCapacity(initialCapacity)
        .maximumSize(maximumSize)
        .expireAfterWrite(Duration.ofMinutes(durationOfMinutes))
        .recordStats();
  }
}
