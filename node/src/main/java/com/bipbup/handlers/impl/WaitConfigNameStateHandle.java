package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitConfigNameStateHandle implements StateHandler {
    private final UserUtil userUtil;

    @Override
    public String process(AppUser appUser, String text) {
        if (text.equals("/cancel")) {
            userUtil.updateUserState(appUser, BASIC_STATE);
            return "Команда была отменена.";
        }

        for (var appUserConfig : appUser.getAppUserConfigs()) {
            if (appUserConfig.getConfigName().equals(text)) {
                userUtil.updateUserState(appUser, BASIC_STATE);
                return String.format("Конфигурация с названием \"%s\" уже существует.", text);
            }
        }

        AppUserConfig config = AppUserConfig.builder()
                .configName(text)
                .appUser(appUser)
                .build();

        appUser.getAppUserConfigs().add(config);
        userUtil.updateUserState(appUser, WAIT_QUERY_STATE);
        log.info("User {} changed state to WAIT_QUERY_STATE", appUser.getFirstName());

        return String.format("Введите запрос для конфигурации \"%s\":", text);
    }
}
