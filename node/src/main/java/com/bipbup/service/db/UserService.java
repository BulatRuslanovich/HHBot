package com.bipbup.service.db;

import com.bipbup.entity.AppUser;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface UserService {

    void deleteAppUser(AppUser user);

    AppUser findOrSaveAppUser(Update update);

    List<AppUser> getAppUsers();
}
