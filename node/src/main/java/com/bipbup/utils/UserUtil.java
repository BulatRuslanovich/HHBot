package com.bipbup.utils;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.AppUserState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.service.impl.APIHandlerImpl.COUNT_OF_DAYS;

@RequiredArgsConstructor
@Component
public class UserUtil {
    private final AppUserDAO appUserDAO;
    private final AppUserConfigDAO appUserConfigDAO;

    public void updateUserState(AppUser appUser, AppUserState state) {
        appUser.setState(state);
        appUserDAO.save(appUser);
    }

    public void updateUserQuery(AppUser appUser, String text) {
        AppUserConfig appUserConfig;
        if (appUser.getAppUserConfigs().isEmpty()) {
            appUserConfig = AppUserConfig.builder()
                    .appUser(appUser)
                    .build();
        } else {
            appUserConfig = appUser.getAppUserConfigs().get(0);
        }

        appUserConfig.setQueryText(text);
        appUserConfig.setConfigName(text);
        appUserConfig.setLastNotificationTime(LocalDateTime.now().minusDays(COUNT_OF_DAYS));
        appUserConfigDAO.save(appUserConfig);

        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
    }

    public AppUser findOrSaveAppUser(Update update) {
        User messageSender;
        if (update.hasMessage())
            messageSender = update.getMessage().getFrom();
        else
            messageSender = update.getCallbackQuery().getFrom();

        var appUserOptional = appUserDAO.findByTelegramId(messageSender.getId());
        if (appUserOptional.isEmpty()) {
            var appUser = AppUser.builder()
                    .telegramId(messageSender.getId())
                    .username(messageSender.getUserName())
                    .firstName(messageSender.getFirstName())
                    .state(BASIC_STATE)
                    .lastName(messageSender.getLastName())
                    .build();

            appUserDAO.save(appUser);

//            var appUserConfig = AppUserConfig.builder()
//                    .appUser(appUser)
//                    .build();
//
//            appUserConfigDAO.save(appUserConfig);

            return appUser;
        }

        return appUserOptional.get();
    }

    public AppUserConfig getAppUserConfigById(long id) {
        var appUserConfigOptional = appUserConfigDAO.findById(id);
        return appUserConfigOptional.orElse(null);
    }
}
