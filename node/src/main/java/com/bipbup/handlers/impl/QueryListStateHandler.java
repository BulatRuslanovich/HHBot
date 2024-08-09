package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.EnumParam;
import com.bipbup.handlers.StateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hashids.Hashids;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.QUERY_MENU_STATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryListStateHandler implements StateHandler {
    protected static final String COMMAND_CANCEL = "/cancel";
    protected static final String COMMAND_MY_QUERIES = "/myqueries";
    protected static final String COMMAND_NEW_QUERY = "/newquery";
    protected static final String PREFIX_QUERY = "query_";
    protected static final String MESSAGE_COMMAND_CANCELLED = "Команда отменена!";
    protected static final String MESSAGE_CONFIGURATION_NOT_FOUND = "Конфигурация не найдена";
    protected static final String QUERY_OUTPUT_FORMAT = """
            Конфигурация "%s" с запросом "%s"
            Что хотите сделать с ней?""";

    private final AppUserDAO appUserDAO;

    private final AppUserConfigDAO appUserConfigDAO;

    private final BasicStateHandler basicStateHandler;

    private final Hashids hashids;

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
            var hash = text.substring(PREFIX_QUERY.length());
            queryId = hashids.decode(hash)[0];
        } catch (NumberFormatException e) {
            log.error("Failed to parse queryId from text: {}", text, e);
            return "";
        }

        var answer = generateQueryOutput(queryId);

        if (Boolean.TRUE.equals(answer.getSecond())) {
            appUser.setState(QUERY_MENU_STATE);
            appUserDAO.saveAndFlush(appUser);
        }

        log.debug("User {} queried configuration with id {} and state set to QUERY_MENU_STATE", appUser.getFirstName(), queryId);
        return answer.getFirst();
    }

    private Pair<String, Boolean> generateQueryOutput(final long configId) {
        Optional<AppUserConfig> optionalAppUserConfig = appUserConfigDAO.findById(configId);

        if (optionalAppUserConfig.isEmpty()) {
            return Pair.of(MESSAGE_CONFIGURATION_NOT_FOUND, false);
        }

        AppUserConfig config = optionalAppUserConfig.get();
        var answer = String.format(QUERY_OUTPUT_FORMAT, config.getConfigName(), config.getQueryText());
        return Pair.of(answer, true);
    }
}
