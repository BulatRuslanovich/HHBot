package com.bipbup.service.impl;

import com.bipbup.service.AreaService;
import com.bipbup.utils.AreaUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
public class AreaServiceImpl implements AreaService {

    @Override
    @Cacheable(value = "areaIds")
    public Integer getAreaIdByName(String areaName) {
        return AreaUtil.getAreaIdFromApi(areaName);
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
