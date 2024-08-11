package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.bipbup.enums.AppUserState.QUERY_LIST_STATE;
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class BasicStateHandler implements StateHandler {

    private final AppUserDAO appUserDAO;

    private final AppUserConfigDAO appUserConfigDAO;

    private Map<String, Function<AppUser, String>> commandHandlers;

    protected static final String WELCOME_MESSAGE = "Добро пожаловать в капитализм, %s!";
    protected static final String QUERY_PROMPT_MESSAGE = "Введите название вашей конфигурации, если хотите отменить команду, пожалуйста, введите /cancel:";
    protected static final String USER_QUERIES_MESSAGE = "Ваши запросы:";
    protected static final String NO_SAVED_QUERIES_MESSAGE = """
                У вас пока нет сохранённых запросов.
                Введите /newquery, чтобы добавить новый запрос.
                """;

    @PostConstruct
    public void init() {
        commandHandlers = new HashMap<>();
        commandHandlers.put("/start", this::startInteraction);
        commandHandlers.put("/newquery", this::addQueryOutput);
        commandHandlers.put("/myqueries", this::showQueriesOutput);
    }

    @Override
    public String process(final AppUser appUser, final String text) {
        return commandHandlers.getOrDefault(text, user -> "").apply(appUser);
    }

    private String startInteraction(final AppUser appUser) {
        var firstName = appUser.getFirstName();
        return String.format(WELCOME_MESSAGE, firstName);
    }

    private String addQueryOutput(final AppUser appUser) {
        if (!WAIT_CONFIG_NAME_STATE.equals(appUser.getState())) {
            appUser.setState(WAIT_CONFIG_NAME_STATE);
            appUserDAO.saveAndFlush(appUser);
            log.debug("User {} changed state to WAIT_CONFIG_NAME_STATE", appUser.getFirstName());
        }
        return QUERY_PROMPT_MESSAGE;
    }

    protected String showQueriesOutput(final AppUser appUser) {
        var appUserConfigs = appUserConfigDAO.findByAppUser(appUser);
        if (appUserConfigs == null || appUserConfigs.isEmpty()) {
            return NO_SAVED_QUERIES_MESSAGE;
        }

        if (!QUERY_LIST_STATE.equals(appUser.getState())) {
            appUser.setState(QUERY_LIST_STATE);
            appUserDAO.saveAndFlush(appUser);
            log.debug("User {} changed state to QUERY_LIST_STATE", appUser.getFirstName());
        }
        return USER_QUERIES_MESSAGE;
    }
}
