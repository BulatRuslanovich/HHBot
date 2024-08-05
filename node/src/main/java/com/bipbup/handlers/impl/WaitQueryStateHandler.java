package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserConfigUtil;
import com.bipbup.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

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

    private final UserConfigUtil userConfigUtil;
    private final UserUtil userUtil;

    @Override
    public String process(AppUser appUser, String query) {
        String encodedQuery = encodeQuery(query);
        List<AppUserConfig> configs = appUser.getAppUserConfigs();
        AppUserConfig lastConfig = getLastConfig(configs);

        if (isCancelCommand(encodedQuery))
            return handleCancelCommand(lastConfig);
        if (!isValidQueryText(encodedQuery))
            return handleInvalidQuery(lastConfig);
        return handleValidQuery(lastConfig, encodedQuery);
    }


    private String encodeQuery(String query) {
        return query.replace("+", "%2B");
    }

    private AppUserConfig getLastConfig(List<AppUserConfig> configs) {
        int size = configs.size();
        return configs.get(size - 1);
    }

    private boolean isCancelCommand(String query) {
        return CANCEL_COMMAND.equals(query);
    }

    private String handleCancelCommand(AppUserConfig lastConfig) {
        var appUser = lastConfig.getAppUser();
        userUtil.updateUserState(appUser, BASIC_STATE);
        userConfigUtil.removeConfig(lastConfig);
        return COMMAND_CANCELLED_MESSAGE;
    }

    private boolean isValidQueryText(String query) {
        return !Objects.isNull(query)
                && !query.trim().isEmpty()
                && query.length() <= MAX_QUERY_LENGTH;
    }

    private String handleInvalidQuery(AppUserConfig lastConfig) {
        var appUser = lastConfig.getAppUser();
        userUtil.updateUserState(appUser, BASIC_STATE);
        userConfigUtil.removeConfig(lastConfig);
        return INVALID_QUERY_MESSAGE;
    }

    private String handleValidQuery(AppUserConfig lastConfig, String query) {
        var appUser = lastConfig.getAppUser();
        userConfigUtil.updateConfigQuery(lastConfig, query);
        userUtil.updateUserState(appUser, BASIC_STATE);
        log.info("User {} set query \"{}\"", appUser.getFirstName(), query);
        return String.format(QUERY_SET_MESSAGE_TEMPLATE, query, lastConfig.getConfigName());
    }
}
