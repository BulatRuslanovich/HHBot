package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

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

    private final Decoder decoder;

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
        var hash = text.substring(PREFIX_QUERY.length());
        var configId = decoder.idOf(hash);
        var answer = generateQueryOutput(configId);

        if (Boolean.TRUE.equals(answer.getSecond())) {
            appUser.setState(QUERY_MENU_STATE);
            appUserDAO.saveAndFlush(appUser);
        }

        log.debug("User {} queried configuration with id {} and state set to QUERY_MENU_STATE", appUser.getFirstName(), configId);
        return answer.getFirst();
    }

    private Pair<String, Boolean> generateQueryOutput(final long configId) {
        var optionalAppUserConfig = appUserConfigDAO.findById(configId);

        if (optionalAppUserConfig.isEmpty()) {
            return Pair.of(MESSAGE_CONFIGURATION_NOT_FOUND, false);
        }

        var config = optionalAppUserConfig.get();
        var answer = String.format(QUERY_OUTPUT_FORMAT, config.getConfigName(), config.getQueryText());
        return Pair.of(answer, true);
    }
}
