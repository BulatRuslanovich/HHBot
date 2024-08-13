package com.bipbup.service;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UserService {
    AppUser findOrSaveAppUser(final Update update);

    AppUserState saveUserState(Long telegramId, AppUserState state);

    AppUserState getUserState(Long telegramId);

    void clearUserState(Long telegramId);
}
