package com.bipbup.service.cache.impl;

import com.bipbup.enums.impl.ScheduleTypeParam;
import com.bipbup.service.cache.ScheduleTypeCacheService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ScheduleTypeCacheServiceImpl implements ScheduleTypeCacheService {

    @Override
    @CachePut(value = "schedule", key = "#telegramId")
    public List<ScheduleTypeParam> putScheduleTypes(long telegramId, ScheduleTypeParam param,
                                                    List<ScheduleTypeParam> params) {
        params.add(param);
        return params;
    }

    @Override
    @CachePut(value = "schedule", key = "#telegramId")
    public List<ScheduleTypeParam> removeScheduleTypes(long telegramId, ScheduleTypeParam param,
                                                               List<ScheduleTypeParam> params) {
        params.remove(param);
        return params;
    }

    @Override
    @Cacheable(value = "schedule")
    public List<ScheduleTypeParam> getScheduleTypes(long telegramId) {
        return new ArrayList<>(0);
    }

    @Override
    @CacheEvict(value = "schedule")
    public void clearScheduleTypes(long telegramId) {
        // clearing cache, doesn't need implementing
    }
}
