package com.bipbup.service.cache;

import com.bipbup.enums.AppUserState;

public interface UserStateCacheService {

    AppUserState putUserState(long telegramId, AppUserState state);

    AppUserState getUserState(long telegramId);

    void clearUserState(long telegramId);
}
