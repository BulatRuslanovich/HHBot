package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.ConfigUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitConfigNameStateHandler implements StateHandler {

    private final AppUserDAO appUserDAO;

    private final AppUserConfigDAO appUserConfigDAO;

    private final ConfigUtil configUtil;

    protected static final int MAX_CONFIG_NAME_LENGTH = 50;
    protected static final String CANCEL_COMMAND = "/cancel";
    protected static final String CANCEL_MESSAGE = "Команда была отменена.";
    protected static final String CONFIG_EXISTS_MESSAGE_TEMPLATE = "Конфигурация с названием \"%s\" уже существует.";
    protected static final String ENTER_QUERY_MESSAGE_TEMPLATE = "Введите запрос для конфигурации \"%s\":";
    protected static final String CONFIG_UPDATED_MESSAGE = "Название конфигурации изменено.";
    protected static final String INVALID_CONFIG_NAME_MESSAGE = "Некорректное название. Пожалуйста, проверьте введенные данные.";
    protected static final String CONFIG_NOT_FOUND_MESSAGE = "Произошла ошибка. Попробуйте ещё раз.";

    @Override
    public String process(final AppUser appUser, final String text) {
        if (isCancelCommand(text)) {
            return handleCancel(appUser);
        } else if (!isValidConfigName(text)) {
            return handleInvalidConfigName(appUser);
        } else if (isConfigExist(appUser, text)) {
            return handleExistingConfig(appUser, text);
        } else if (isConfigUpdating(appUser)) {
            return handleUpdatingConfig(appUser, text);
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

    private boolean isValidConfigName(final String configName) {
        return configName != null && !configName.trim().isEmpty() && configName.length() <= MAX_CONFIG_NAME_LENGTH;
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

    private boolean isConfigUpdating(final AppUser appUser) {
        var configId = configUtil.getSelectedConfigId(appUser.getTelegramId());
        return configId != null;
    }

    private String handleUpdatingConfig(final AppUser appUser, final String configName) {
        var configId = configUtil.getSelectedConfigId(appUser.getTelegramId());
        configUtil.clearConfigSelection(appUser.getTelegramId());
        return appUserConfigDAO.findById(configId)
                .map(config -> updateConfigAndUserState(appUser, config, configName))
                .orElse(getConfigNotFoundMessage(appUser, configId));
    }

    private String updateConfigAndUserState(final AppUser appUser, final AppUserConfig config, final String configName) {
        config.setConfigName(configName);
        appUserConfigDAO.saveAndFlush(config);
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.debug("User {} updated config \"{}\" and state set to BASIC_STATE", appUser, configName);
        return CONFIG_UPDATED_MESSAGE;
    }

    private String getConfigNotFoundMessage(final AppUser appUser, final Long configId) {
        configUtil.clearConfigSelection(appUser.getTelegramId());
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.warn("Config with id {} not found for user {}", configId, appUser.getTelegramId());
        return CONFIG_NOT_FOUND_MESSAGE;
    }

    private String handleNewConfig(final AppUser appUser, final String configName) {
        AppUserConfig newConfig = createConfigWithOnlyName(appUser, configName);
        appUserConfigDAO.saveAndFlush(newConfig);
        appUser.setState(WAIT_QUERY_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.debug("User {} created config \"{}\" and state set to WAIT_QUERY_STATE", appUser, configName);
        return String.format(ENTER_QUERY_MESSAGE_TEMPLATE, configName);
    }

    private AppUserConfig createConfigWithOnlyName(final AppUser appUser, final String configName) {
        return AppUserConfig.builder()
                .configName(configName)
                .appUser(appUser)
                .build();
    }

    private String handleInvalidConfigName(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.debug("User {} provided an invalid config name.", appUser.getFirstName());
        return INVALID_CONFIG_NAME_MESSAGE;
    }
}
