package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserConfigUtil;
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
    protected static final String CONFIG_EXISTS_MESSAGE_TEMPLATE =
            "Конфигурация с названием \"%s\" уже существует.";
    protected static final String ENTER_QUERY_MESSAGE_TEMPLATE =
            "Введите запрос для конфигурации \"%s\":";

    private final UserUtil userUtil;
    private final UserConfigUtil userConfigUtil;
    private final AppUserConfigDAO appUserConfigDAO;

    @Override
    public String process(final AppUser appUser, final String text) {
        if (isCancelCommand(text)) {
            return handleCancel(appUser);
        } else if (isConfigExist(appUser, text)) {
            return handleExistingConfig(appUser, text);
        } else {
            return handleNewConfig(appUser, text);
        }
    }

    private boolean isCancelCommand(final String text) {
        return CANCEL_COMMAND.equals(text);
    }

    private String handleCancel(final AppUser appUser) {
        userUtil.updateUserState(appUser, BASIC_STATE);
        return CANCEL_MESSAGE;
    }

    private boolean isConfigExist(final AppUser appUser,
                                  final String configName) {
        return appUser.getAppUserConfigs().stream()
                .anyMatch(c -> c.getConfigName().equals(configName));
    }

    private String handleExistingConfig(final AppUser appUser,
                                        final String configName) {
        userUtil.updateUserState(appUser, BASIC_STATE);
        return String.format(CONFIG_EXISTS_MESSAGE_TEMPLATE, configName);
    }

    private String handleNewConfig(final AppUser appUser,
                                   final String configName) {
        AppUserConfig newConfig = createConfigWithOnlyName(appUser, configName);
        appUserConfigDAO.save(newConfig);
        userUtil.updateUserState(appUser, WAIT_QUERY_STATE);
        log.info("User {} changed state to WAIT_QUERY_STATE",
                appUser.getFirstName());

        return String.format(ENTER_QUERY_MESSAGE_TEMPLATE, configName);
    }

    private AppUserConfig createConfigWithOnlyName(final AppUser appUser,
                                                   final String configName) {
        return AppUserConfig.builder()
                .configName(configName)
                .appUser(appUser)
                .build();
    }
}
