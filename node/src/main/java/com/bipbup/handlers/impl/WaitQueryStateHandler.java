package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.bipbup.enums.AppUserState.BASIC_STATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitQueryStateHandler implements StateHandler {
    private static final int MAX_QUERY_LENGTH = 50;
    private static final String CANCEL_COMMAND = "/cancel";
    private static final String COMMAND_CANCELLED_MESSAGE = "Команда была отменена.";
    private static final String INVALID_QUERY_MESSAGE = "Некорректный запрос. Пожалуйста, проверьте введенные данные.";
    private static final String QUERY_SET_MESSAGE_TEMPLATE = "Запрос \"%s\" успешно установлен в конфигурации \"%s\".";

    private final AppUserConfigDAO appUserConfigDAO;
    private final AppUserDAO appUserDAO;

    @Override
    public String process(final AppUser appUser, final String query) {
        List<AppUserConfig> configs = appUserConfigDAO.findByAppUser(appUser);
        AppUserConfig lastConfig = getLastConfig(configs);

        if (isCancelCommand(query)) {
            return handleCancelCommand(appUser, lastConfig);
        } else if (!isValidQueryText(query)) {
            return handleInvalidQuery(appUser, lastConfig);
        } else {
            return handleValidQuery(appUser, lastConfig, query);
        }
    }

    private AppUserConfig getLastConfig(final List<AppUserConfig> configs) {
        if (configs.isEmpty()) {
            log.error("No configurations found for user.");
            throw new IllegalStateException("No configurations found for user.");
        }
        return configs.get(configs.size() - 1);
    }

    private boolean isCancelCommand(final String query) {
        return CANCEL_COMMAND.equals(query);
    }

    private String handleCancelCommand(final AppUser appUser, final AppUserConfig lastConfig) {
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        appUserConfigDAO.delete(lastConfig);
        log.debug("User {} cancelled the command. Configuration '{}' deleted.", appUser.getFirstName(), lastConfig.getConfigName());
        return COMMAND_CANCELLED_MESSAGE;
    }

    private boolean isValidQueryText(final String query) {
        return query != null && !query.trim().isEmpty() && query.length() <= MAX_QUERY_LENGTH;
    }

    private String handleInvalidQuery(final AppUser appUser, final AppUserConfig lastConfig) {
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        appUserConfigDAO.delete(lastConfig);
        log.debug("User {} provided an invalid query. Configuration '{}' deleted.", appUser.getFirstName(), lastConfig.getConfigName());
        return INVALID_QUERY_MESSAGE;
    }

    private String handleValidQuery(final AppUser appUser, final AppUserConfig lastConfig, final String query) {
        lastConfig.setQueryText(query);
        appUserConfigDAO.saveAndFlush(lastConfig);
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.info("User {} set query '{}' in configuration '{}'", appUser.getFirstName(), query, lastConfig.getConfigName());
        return String.format(QUERY_SET_MESSAGE_TEMPLATE, query, lastConfig.getConfigName());
    }
}
