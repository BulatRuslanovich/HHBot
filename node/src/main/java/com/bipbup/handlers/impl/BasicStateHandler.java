package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.QUERY_LIST_STATE;
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;

@Slf4j
@RequiredArgsConstructor
@Component
public class BasicStateHandler implements StateHandler {
    protected static final String WELCOME_MESSAGE =
            "Добро пожаловать в капитализм, %s!";
    protected static final String QUERY_PROMPT_MESSAGE =
            "Введите название вашей конфигурации, "
                    + "если хотите отменить команду,"
                    + " пожалуйста, введите /cancel:";
    protected static final String USER_QUERIES_MESSAGE =
            "Ваши запросы:";
    protected static final String NO_SAVED_QUERIES_MESSAGE =
                    """
                    У вас пока нет сохранённых запросов.
                    Введите /newquery, чтобы добавить новый запрос.
                    """;

    private final UserUtil userUtil;
    private final AppUserConfigDAO appUserConfigDAO;

    @Override
    public String process(final AppUser appUser, final String text) {
        return switch (text) {
            case "/start" -> startInteraction(appUser);
            case "/newquery" -> addQueryOutput(appUser);
            case "/myqueries" -> showQueriesOutput(appUser);
            default -> "";
        };
    }

    private String startInteraction(final AppUser appUser) {
        var firstName = appUser.getFirstName();
        return String.format(WELCOME_MESSAGE, firstName);
    }

    private String addQueryOutput(final AppUser appUser) {
        userUtil.updateUserState(appUser, WAIT_CONFIG_NAME_STATE);
        log.info("User {} changed state to WAIT_CONFIG_NAME_STATE",
                appUser.getFirstName());
        return QUERY_PROMPT_MESSAGE;
    }

    protected String showQueriesOutput(final AppUser appUser) {
        var appUserConfigs = appUserConfigDAO.findByAppUser(appUser);
        if (appUserConfigs == null || appUserConfigs.isEmpty()) {
            return NO_SAVED_QUERIES_MESSAGE;
        }

        userUtil.updateUserState(appUser, QUERY_LIST_STATE);
        log.info("User {} changed state to QUERY_LIST_STATE",
                appUser.getFirstName());
        return USER_QUERIES_MESSAGE;
    }
}
