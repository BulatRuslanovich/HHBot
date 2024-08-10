package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.StateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.BASIC_STATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitConfigNameStateHandler implements StateHandler {
    protected static final String CANCEL_COMMAND = "/cancel";
    protected static final String CANCEL_MESSAGE = "Команда была отменена.";
    protected static final String CONFIG_EXISTS_MESSAGE_TEMPLATE =
            "Конфигурация с названием \"%s\" уже существует.";
//    protected static final String ENTER_QUERY_MESSAGE_TEMPLATE =
//            "Введите запрос для конфигурации \"%s\":";
    protected static final String CONFIG_CREATED_MESSAGE_TEMPLATE =
            "Конфигурация с названием \"%s\" создана.";

    private final AppUserDAO appUserDAO;

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
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.debug("User {} cancelled the command and state set to BASIC_STATE", appUser.getFirstName());
        return CANCEL_MESSAGE;
    }

    private boolean isConfigExist(final AppUser appUser, final String configName) {
        var configs = appUserConfigDAO.findByAppUser(appUser);
        return configs.stream().anyMatch(config -> config.getConfigName().equals(configName));
    }

    private String handleExistingConfig(final AppUser appUser, final String configName) {
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.debug("User {} attempted to create an existing config '{}'", appUser.getFirstName(), configName);
        return String.format(CONFIG_EXISTS_MESSAGE_TEMPLATE, configName);
    }

    private String handleNewConfig(final AppUser appUser, final String configName) {
        AppUserConfig newConfig = createConfigWithOnlyName(appUser, configName);
        appUserConfigDAO.save(newConfig);
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.debug("User {} created config \"{}\" and state set to BASIC_STATE", appUser, configName);
        return String.format(CONFIG_CREATED_MESSAGE_TEMPLATE, configName);

//        appUser.setState(WAIT_QUERY_STATE);
//        appUserDAO.saveAndFlush(appUser);
//        log.debug("User {} changed state to WAIT_QUERY_STATE", appUser.getFirstName());
//        return String.format(ENTER_QUERY_MESSAGE_TEMPLATE, configName);
    }

    private AppUserConfig createConfigWithOnlyName(final AppUser appUser, final String configName) {
        return AppUserConfig.builder()
                .configName(configName)
                .appUser(appUser)
                .build();
    }

    // TODO: leave here only name setting, move config creation to another handler
}
