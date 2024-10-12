package com.bipbup.service.cache.impl;

import com.bipbup.service.cache.ConfigCacheService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ConfigCacheServiceImpl implements ConfigCacheService {

    @Override
    @CachePut(value = "config", key = "#telegramId")
    public Long putConfigId(long telegramId, long configId) {
        return configId;
    }

    @Override
    @Cacheable(value = "config")
    public Long getConfigId(long telegramId) {
        return null;
    }

    @Override
    @CacheEvict(value = "config")
    public void clearConfigId(long telegramId) {
        // clearing cache, doesn't need implementing
    }
}
