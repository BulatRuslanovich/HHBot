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

import java.util.List;

import static com.bipbup.enums.AppUserState.BASIC_STATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitQueryStateHandler implements StateHandler {
    protected static final int MAX_QUERY_LENGTH = 50;
    protected static final String CANCEL_COMMAND = "/cancel";
    protected static final String CANCEL_MESSAGE = "Команда была отменена.";
    protected static final String INVALID_QUERY_MESSAGE = "Некорректный запрос. Пожалуйста, проверьте введенные данные.";
    protected static final String QUERY_SET_MESSAGE_TEMPLATE = "Запрос \"%s\" успешно установлен в конфигурации \"%s\".";
    protected static final String CONFIG_NOT_FOUND_MESSAGE = "Произошла ошибка. Попробуйте ещё раз.";

    private final AppUserConfigDAO appUserConfigDAO;
    private final AppUserDAO appUserDAO;
    private final ConfigUtil configUtil;

    @Override
    public String process(final AppUser appUser, final String query) {
        if (isCancelCommand(query)) {
            return handleCancel(appUser);
        } else if (!isValidQueryText(query)) {
            return handleInvalidQuery(appUser);
        } else {
            AppUserConfig config;
            if (isConfigUpdating(appUser)) {
                var configId = configUtil.getSelectedConfigId(appUser.getTelegramId());
                var configOptional = appUserConfigDAO.findById(configId);
                if (configOptional.isPresent()) {
                    config = configOptional.get();
                    configUtil.clearConfigSelection(appUser.getTelegramId());
                } else {
                    return getConfigNotFoundMessage(appUser, configId);
                }
            } else {
                config = getLastConfig(appUser);
            }
            return handleValidQuery(appUser, config, query);
        }
    }

    private boolean isConfigUpdating(final AppUser appUser) {
        Long configId = configUtil.getSelectedConfigId(appUser.getTelegramId());
        return configId != null;
    }

    private String getConfigNotFoundMessage(final AppUser appUser, final Long configId) {
        configUtil.clearConfigSelection(appUser.getTelegramId());
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.warn("Config with id {} not found for user {}", configId, appUser.getTelegramId());
        return CONFIG_NOT_FOUND_MESSAGE;
    }

    private AppUserConfig getLastConfig(final AppUser appUser) {
        List<AppUserConfig> configs = appUserConfigDAO.findByAppUser(appUser);
        if (configs.isEmpty()) {
            log.error("No configurations found for user.");
            throw new IllegalStateException("No configurations found for user.");
        }
        return configs.get(configs.size() - 1);
    }

    private boolean isCancelCommand(final String query) {
        return CANCEL_COMMAND.equals(query);
    }

    private String handleCancel(final AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.debug("User {} cancelled the command and state set to BASIC_STATE.", appUser.getFirstName());
        return CANCEL_MESSAGE;
    }

    private boolean isValidQueryText(final String query) {
        return query != null && !query.trim().isEmpty() && query.length() <= MAX_QUERY_LENGTH;
    }

    private String handleInvalidQuery(final AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.debug("User {} provided an invalid query and state set to BASIC_STATE.", appUser.getFirstName());
        return INVALID_QUERY_MESSAGE;
    }

    private String handleValidQuery(final AppUser appUser, final AppUserConfig config, final String query) {
        config.setQueryText(query);
        appUserConfigDAO.saveAndFlush(config);
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.info("User {} set query '{}' in configuration '{}'", appUser.getFirstName(), query, config.getConfigName());
        return String.format(QUERY_SET_MESSAGE_TEMPLATE, query, config.getConfigName());
    }
}
