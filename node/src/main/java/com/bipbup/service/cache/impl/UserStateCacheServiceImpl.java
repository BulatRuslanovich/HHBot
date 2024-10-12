package com.bipbup.service.cache.impl;

import com.bipbup.enums.AppUserState;
import static com.bipbup.enums.AppUserState.BASIC_STATE;
import com.bipbup.service.cache.UserStateCacheService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserStateCacheServiceImpl implements UserStateCacheService {

    @Override
    @CachePut(value = "userStates", key = "#telegramId")
    public AppUserState putUserState(long telegramId, AppUserState state) {
        return state;
    }

    @Override
    @Cacheable(value = "userStates")
    public AppUserState getUserState(long telegramId) {
        return BASIC_STATE;
    }

    @Override
    @CacheEvict(value = "userStates")
    public void clearUserState(long telegramId) {
        // clearing cache, doesn't need implementing
    }
}
