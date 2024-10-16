package com.bipbup.service.cache.impl;

import com.bipbup.service.cache.AreaCacheService;
import com.bipbup.utils.AreaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AreaCacheCacheServiceImpl implements AreaCacheService {

    private final AreaUtil areaUtil;

    @Override
    @Cacheable(value = "areaIds")
    public Integer getAreaIdByName(String areaName) {
        return areaUtil.getAreaIdFromApi(areaName);
    }

    @Override
    @Scheduled(fixedRateString = "${area.update-period}")
    public void updateAreaIds() {
        clearAllAreaIds();
    }

    @CacheEvict(value = "areaIds")
    public void clearAllAreaIds() {
        // clearing cache, doesn't need implementing
    }
}
