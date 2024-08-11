package com.bipbup.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConfigUtil {
    @CachePut(value = "configSelections", key = "#telegramId")
    public Long saveConfigSelection(Long telegramId, Long configId) {
        return configId;
    }

    @Cacheable(value = "configSelections")
    public Long getSelectedConfigId(Long telegramId) {
        log.debug("No configuration selected for user with telegramId: " + telegramId);
        return null;
    }

    @CacheEvict(value = "configSelections")
    public void clearConfigSelection(Long telegramId) {}
}
