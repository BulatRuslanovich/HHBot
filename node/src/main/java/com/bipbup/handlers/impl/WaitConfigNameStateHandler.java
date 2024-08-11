package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.Cancellable;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.ConfigUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;

@Slf4j
@Component
public class WaitConfigNameStateHandler extends Cancellable implements StateHandler {

    private final AppUserConfigDAO appUserConfigDAO;

    private final ConfigUtil configUtil;

    protected static final int MAX_CONFIG_NAME_LENGTH = 50;
    protected static final String CONFIG_EXISTS_MESSAGE_TEMPLATE = "Конфигурация с названием \"%s\" уже существует.";
    protected static final String ENTER_QUERY_MESSAGE_TEMPLATE = "Введите запрос для конфигурации \"%s\":";
    protected static final String CONFIG_UPDATED_MESSAGE = "Название конфигурации изменено.";
    protected static final String INVALID_CONFIG_NAME_MESSAGE = "Некорректное название. Пожалуйста, проверьте введенные данные.";
    protected static final String CONFIG_NOT_FOUND_MESSAGE = "Произошла ошибка. Попробуйте ещё раз.";

    public WaitConfigNameStateHandler(AppUserDAO appUserDAO,
                                      BasicStateHandler basicStateHandler,
                                      AppUserConfigDAO appUserConfigDAO,
                                      ConfigUtil configUtil) {
        super(appUserDAO, basicStateHandler);
        this.appUserConfigDAO = appUserConfigDAO;
        this.configUtil = configUtil;
    }


    @Override
    public String process(final AppUser user, final String input) {
        if (isCancelCommand(input)) return processCancelCommand(user);
        if (isBasicCommand(input)) return processBasicCommand(user, input);
        if (isInvalidConfigName(input)) return processInvalidConfigName(user);
        if (isConfigExist(user, input)) return processExistingConfig(user, input);
        if (isConfigUpdating(user)) return processUpdatingConfig(user, input);

        return processNewConfig(user, input);
    }

    private boolean isInvalidConfigName(final String configName) {
        return !(configName != null && !configName.trim().isEmpty() && configName.length() <= MAX_CONFIG_NAME_LENGTH);
    }

    private boolean isConfigExist(final AppUser user, final String configName) {
        var configs = appUserConfigDAO.findByAppUser(user);
        return configs.stream().anyMatch(config -> config.getConfigName().equals(configName));
    }

    private boolean isConfigUpdating(final AppUser user) {
        var configId = configUtil.getSelectedConfigId(user.getTelegramId());
        return configId != null;
    }

    private AppUserConfig createConfigWithOnlyName(final AppUser user, final String configName) {
        return AppUserConfig.builder()
                .configName(configName)
                .appUser(user)
                .build();
    }

    private String updateConfigName(final AppUser user,
                                    final AppUserConfig config,
                                    final String configName) {
        config.setConfigName(configName);
        appUserConfigDAO.saveAndFlush(config);
        user.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(user);
        log.debug("User {} updated config \"{}\" and state set to BASIC_STATE", user, configName);
        return CONFIG_UPDATED_MESSAGE;
    }

    private String processExistingConfig(final AppUser user, final String configName) {
        user.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(user);
        log.debug("User {} attempted to create an existing config '{}'", user.getFirstName(), configName);
        return String.format(CONFIG_EXISTS_MESSAGE_TEMPLATE, configName);
    }

    private String processUpdatingConfig(final AppUser user, final String configName) {
        var configId = configUtil.getSelectedConfigId(user.getTelegramId());
        configUtil.clearConfigSelection(user.getTelegramId());
        return appUserConfigDAO.findById(configId)
                .map(config -> updateConfigName(user, config, configName))
                .orElse(processConfigNotFoundMessage(user, configId));
    }

    private String processConfigNotFoundMessage(final AppUser user, final Long configId) {
        configUtil.clearConfigSelection(user.getTelegramId());
        user.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(user);
        log.warn("Config with id {} not found for user {}", configId, user.getTelegramId());
        return CONFIG_NOT_FOUND_MESSAGE;
    }

    private String processNewConfig(final AppUser user, final String configName) {
        AppUserConfig newConfig = createConfigWithOnlyName(user, configName);
        appUserConfigDAO.saveAndFlush(newConfig);
        user.setState(WAIT_QUERY_STATE);
        appUserDAO.saveAndFlush(user);
        log.debug("User {} created config \"{}\" and state set to WAIT_QUERY_STATE", user, configName);
        return String.format(ENTER_QUERY_MESSAGE_TEMPLATE, configName);
    }

    private String processInvalidConfigName(AppUser user) {
        user.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(user);
        log.debug("User {} provided an invalid config name.", user.getFirstName());
        return INVALID_CONFIG_NAME_MESSAGE;
    }
}
