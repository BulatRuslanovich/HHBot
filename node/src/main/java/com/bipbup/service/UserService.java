package com.bipbup.service;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UserService {

    AppUser findOrSaveAppUser(final Update update);

    AppUserState saveUserState(long telegramId, final AppUserState state);

    AppUserState getUserState(long telegramId);

    void clearUserState(long telegramId);
}
