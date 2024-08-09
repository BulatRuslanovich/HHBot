package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.EnumParam;
import com.bipbup.handlers.StateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.QUERY_MENU_STATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryListStateHandler implements StateHandler {
    private static final String COMMAND_CANCEL = "/cancel";
    private static final String COMMAND_MY_QUERIES = "/myqueries";
    private static final String COMMAND_NEW_QUERY = "/newquery";
    private static final String PREFIX_QUERY = "query_";

    private static final String MESSAGE_COMMAND_CANCELLED = "Команда отменена!";
    private static final String MESSAGE_CONFIGURATION_NOT_FOUND = "Конфигурация не найдена";
    private static final String QUERY_OUTPUT_FORMAT = """
            Конфигурация "%s" с запросом "%s"
            Что хотите сделать с ней?""";

    private final AppUserDAO appUserDAO;
    private final AppUserConfigDAO appUserConfigDAO;
    private final BasicStateHandler basicStateHandler;

    @Override
    public String process(AppUser appUser, String text) {
        if (COMMAND_CANCEL.equals(text)) {
            return cancelCommand(appUser);
        } else if (text.startsWith(PREFIX_QUERY)) {
            return handleQueryCommand(appUser, text);
        } else if (COMMAND_MY_QUERIES.equals(text) || COMMAND_NEW_QUERY.equals(text)) {
            return basicStateHandler.process(appUser, text);
        }
        return "";
    }

    private String cancelCommand(AppUser appUser) {
        appUser.setState(BASIC_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.debug("User {} cancelled the command and state set to BASIC_STATE", appUser.getFirstName());
        return MESSAGE_COMMAND_CANCELLED;
    }

    private String handleQueryCommand(AppUser appUser, String text) {
        long queryId;
        try {
            queryId = Long.parseLong(text.substring(PREFIX_QUERY.length()));
        } catch (NumberFormatException e) {
            log.error("Failed to parse queryId from text: {}", text, e);
            return "";
        }

        appUser.setState(QUERY_MENU_STATE);
        appUserDAO.saveAndFlush(appUser);
        log.debug("User {} queried configuration with id {} and state set to QUERY_MENU_STATE", appUser.getFirstName(), queryId);
        return generateQueryOutput(queryId);
    }

    private String generateQueryOutput(final long configId) {
        Optional<AppUserConfig> optionalAppUserConfig = appUserConfigDAO.findById(configId);

        if (optionalAppUserConfig.isEmpty()) {
            return MESSAGE_CONFIGURATION_NOT_FOUND;
        }

        AppUserConfig config = optionalAppUserConfig.get();
        return String.format(QUERY_OUTPUT_FORMAT, config.getConfigName(), config.getQueryText());
    }
}
