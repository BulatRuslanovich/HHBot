package com.bipbup.service.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.enums.Role;
import com.bipbup.service.UserService;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final AppUserDAO appUserDAO;

    private final AppUserConfigDAO appUserConfigDAO;

    @Override
    @Transactional
    public void deleteAppUser(AppUser user) {
        appUserConfigDAO.deleteAllByAppUser(user);
        appUserDAO.delete(user);
    }

    @Override
    @Transactional
    public AppUser findOrSaveAppUser(Update update) {
        var sender = update.hasMessage()
                ? update.getMessage().getFrom()
                : update.getCallbackQuery().getFrom();
        var optionalUser = appUserDAO.findByTelegramId(sender.getId());

        if (optionalUser.isPresent()) {
            var user = optionalUser.get();

            if (!Objects.equals(user.getUsername(), sender.getUserName())) {
                user.setUsername(sender.getUserName());
            }

            if (!Objects.equals(user.getFirstName(), sender.getFirstName())) {
                user.setFirstName(sender.getFirstName());
            }

            if (!Objects.equals(user.getLastName(), sender.getLastName())) {
                user.setLastName(sender.getLastName());
            }

            return appUserDAO.save(user);
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
                .build();

        appUserDAO.save(appUser);
        return appUser;
    }

    @Override
    public List<AppUser> getAppUsers() {
        return appUserDAO.findAll();
    }
}
