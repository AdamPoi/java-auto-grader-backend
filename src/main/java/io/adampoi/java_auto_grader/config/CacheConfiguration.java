package io.adampoi.java_auto_grader.config;

import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public org.springframework.cache.CacheManager cacheManager() {
        CachingProvider provider = Caching.getCachingProvider("org.ehcache.jsr107.EhcacheCachingProvider");
        CacheManager cacheManager = provider.getCacheManager();

        // Default cache configuration
        javax.cache.configuration.Configuration<Object, Object> defaultConfig = Eh107Configuration.fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder.newCacheConfigurationBuilder(
                                Object.class, Object.class,
                                ResourcePoolsBuilder.newResourcePoolsBuilder()
                                        .heap(1000, EntryUnit.ENTRIES)
                                        .offheap(10, MemoryUnit.MB)
                        )
                        .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(30)))
                        .build()
        );

        // Create your caches programmatically
        createCacheIfNotExists(cacheManager, "assessmentCache", defaultConfig);
        createCacheIfNotExists(cacheManager, "timedAssessmentCache", defaultConfig);
        createCacheIfNotExists(cacheManager, "userCache", defaultConfig);
        // Add more caches as needed

        return new JCacheCacheManager(cacheManager);
    }

    private void createCacheIfNotExists(CacheManager cacheManager, String cacheName,
                                        javax.cache.configuration.Configuration<Object, Object> config) {
        if (cacheManager.getCache(cacheName) == null) {
            cacheManager.createCache(cacheName, config);
        }
    }
}