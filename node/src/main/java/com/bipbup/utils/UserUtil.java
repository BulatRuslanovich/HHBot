package com.bipbup.utils;

import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static com.bipbup.enums.AppUserState.BASIC_STATE;

@RequiredArgsConstructor
@Component
@Transactional
public class UserUtil {
    private final AppUserDAO appUserDAO;

    public AppUser findOrSaveAppUser(final Update update) {
        var messageSender = update.hasMessage()
                ? update.getMessage().getFrom()
                : update.getCallbackQuery().getFrom();
        var appUserOptional =
                appUserDAO.findByTelegramId(messageSender.getId());
        return appUserOptional.orElseGet(() -> saveAppUser(messageSender));
    }

    private AppUser saveAppUser(final User messageSender) {
        var appUser = AppUser.builder()
                .telegramId(messageSender.getId())
                .username(messageSender.getUserName())
                .firstName(messageSender.getFirstName())
                .lastName(messageSender.getLastName())
                .state(BASIC_STATE)
                .build();

        appUserDAO.save(appUser);
        return appUser;
    }
}
