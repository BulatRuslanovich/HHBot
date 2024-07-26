package com.bipbup.utils;

import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.service.impl.APIHandlerImpl.COUNT_OF_DAYS;

@RequiredArgsConstructor
@Component
public class UserUtil {
    private final AppUserDAO appUserDAO;

    public void updateUserState(AppUser appUser, AppUserState state) {
        appUser.setState(state);
        appUserDAO.save(appUser);
    }

    public void updateUserQuery(AppUser appUser, String text) {
        appUser.setQueryText(text);
        appUser.setLastNotificationTime(LocalDateTime.now().minusDays(COUNT_OF_DAYS));
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
    }

    public AppUser findOrSaveAppUser(Update update) {
        var messageSender = update.getMessage().getFrom();
        var appUserOptional = appUserDAO.findByTelegramId(messageSender.getId());

        if (appUserOptional.isEmpty()) {
            var appUser = AppUser.builder()
                    .telegramId(messageSender.getId())
                    .username(messageSender.getUserName())
                    .firstName(messageSender.getFirstName())
                    .state(BASIC_STATE)
                    .lastName(messageSender.getLastName())
                    .build();

            return appUserDAO.save(appUser);
        }

        return appUserOptional.get();
    }
}
