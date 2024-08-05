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
    protected static final String CANCEL_COMMAND = "/cancel";
    protected static final String CANCEL_MESSAGE = "Команда была отменена.";
    protected static final String CONFIG_EXISTS_MESSAGE_TEMPLATE = "Конфигурация с названием \"%s\" уже существует.";
    protected static final String ENTER_QUERY_MESSAGE_TEMPLATE = "Введите запрос для конфигурации \"%s\":";

    private final UserUtil userUtil;

    @Override
    public String process(AppUser appUser, String text) {
        if (isCancelCommand(text))
            return handleCancel(appUser);
        if (isConfigExist(appUser, text))
            return handleExistingConfig(appUser, text);
        return handleNewConfig(appUser, text);
    }

    private boolean isCancelCommand(String text) {
        return CANCEL_COMMAND.equals(text);
    }

    private String handleCancel(AppUser appUser) {
        userUtil.updateUserState(appUser, BASIC_STATE);
        return CANCEL_MESSAGE;
    }

    private boolean isConfigExist(AppUser appUser, String configName) {
        return appUser.getAppUserConfigs().stream()
                .anyMatch(c -> c.getConfigName().equals(configName));
    }

    private String handleExistingConfig(AppUser appUser, String configName) {
        userUtil.updateUserState(appUser, BASIC_STATE);
        return String.format(CONFIG_EXISTS_MESSAGE_TEMPLATE, configName);
    }

    private String handleNewConfig(AppUser appUser, String configName) {
        AppUserConfig newConfig = createConfigWithOnlyName(appUser, configName);
        appUser.getAppUserConfigs().add(newConfig);

        //так как метод использует save юзера, то конфиг автоматом сохраняется в бд
        userUtil.updateUserState(appUser, WAIT_QUERY_STATE);
        log.info("User {} changed state to WAIT_QUERY_STATE", appUser.getFirstName());

        return String.format(ENTER_QUERY_MESSAGE_TEMPLATE, configName);
    }

    private AppUserConfig createConfigWithOnlyName(AppUser appUser, String configName) {
        return AppUserConfig.builder()
                .configName(configName)
                .appUser(appUser)
                .build();
    }
}
