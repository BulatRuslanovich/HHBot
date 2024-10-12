package com.bipbup.service.cache.impl;

import com.bipbup.enums.impl.EducationLevelParam;
import com.bipbup.service.cache.EducationLevelCacheService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class EducationLevelCacheServiceImpl implements EducationLevelCacheService {

    @Override
    @CachePut(value = "education", key = "#telegramId")
    public List<EducationLevelParam> putEducationLevels(long telegramId, EducationLevelParam param,
                                                        List<EducationLevelParam> params) {
        params.add(param);
        return params;
    }

    @Override
    @CachePut(value = "education", key = "#telegramId")
    public List<EducationLevelParam> removeEducationLevels(long telegramId, EducationLevelParam param,
                                                                   List<EducationLevelParam> params) {
        params.remove(param);
        return params;
    }

    @Override
    @Cacheable(value = "education")
    public List<EducationLevelParam> getEducationLevels(long telegramId) {
        return new ArrayList<>(0);
    }

    @Override
    @CacheEvict(value = "education")
    public void clearEducationLevels(long telegramId) {
        // clearing cache, doesn't need implementing
    }
}
