package io.adampoi.java_auto_grader.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.net.URI;

@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager() {
        try {
            CachingProvider provider = Caching.getCachingProvider();

            // Load ehcache.xml from classpath
            URI ehcacheUri = new ClassPathResource("ehcache.xml").getURI();
            javax.cache.CacheManager cacheManager = provider.getCacheManager(ehcacheUri, getClass().getClassLoader());

            return new JCacheCacheManager(cacheManager);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize cache manager", e);
        }
    }
}