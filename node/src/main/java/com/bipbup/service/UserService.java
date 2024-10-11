package com.bipbup.service;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface UserService {

    void deleteUser(AppUser user);

    AppUser findOrSaveAppUser(Update update);

    AppUserState saveUserState(long telegramId, AppUserState state);

    AppUserState getUserState(long telegramId);

    List<AppUser> getAllUsers();

    void clearUserState(long telegramId);
}
