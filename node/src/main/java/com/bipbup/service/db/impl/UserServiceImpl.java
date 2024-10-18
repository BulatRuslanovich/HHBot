package com.bipbup.service.db.impl;

import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.enums.Role;
import com.bipbup.service.db.UserService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final AppUserDAO appUserDAO;

    @Override
    public void deactivate(AppUser user) {
        user.setActive(false);
        appUserDAO.save(user);
    }

    @Override
    public AppUser findOrSaveAppUser(Update update) {
        var sender = update.hasMessage()
                ? update.getMessage().getFrom()
                : update.getCallbackQuery().getFrom();
        var optionalUser = appUserDAO.findByTelegramId(sender.getId());

        if (optionalUser.isPresent()) {
            var user = optionalUser.get();
            boolean needUpdate = false;

            if (!Objects.equals(user.getUsername(), sender.getUserName())) {
                user.setUsername(sender.getUserName());
                needUpdate = true;
            }

            if (!Objects.equals(user.getFirstName(), sender.getFirstName())) {
                user.setFirstName(sender.getFirstName());
                needUpdate = true;
            }

            if (!Objects.equals(user.getLastName(), sender.getLastName())) {
                user.setLastName(sender.getLastName());
                needUpdate = true;
            }

            if (!user.getActive()) {
                user.setActive(true);
                needUpdate = true;
            }

            if (needUpdate)
                return appUserDAO.save(user);
            return user;
        }

        return saveAppUser(sender);
    }

    private AppUser saveAppUser(User sender) {
        var appUser = AppUser.builder()
                .telegramId(sender.getId())
                .username(sender.getUserName())
                .firstName(sender.getFirstName())
                .lastName(sender.getLastName())
                .role(Role.USER)
                .active(true)
                .build();

        appUserDAO.save(appUser);
        return appUser;
    }

    @Override
    public List<AppUser> getAppUsers() {
        return appUserDAO.findAll();
    }
}
