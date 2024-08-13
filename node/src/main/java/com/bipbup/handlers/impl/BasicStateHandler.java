package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.QUERY_LIST_STATE;
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import static com.bipbup.utils.CommandMessageConstants.MYQUERIES_COMMAND;
import static com.bipbup.utils.CommandMessageConstants.NEWQUERY_COMMAND;
import static com.bipbup.utils.CommandMessageConstants.NO_SAVED_QUERIES_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.QUERY_PROMPT_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.START_COMMAND;
import static com.bipbup.utils.CommandMessageConstants.USER_QUERIES_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.WELCOME_MESSAGE;

@Slf4j
@RequiredArgsConstructor
@Component
public class BasicStateHandler implements StateHandler {

    private final UserService userService;

    private final ConfigService configService;

    @Override
    public String process(final AppUser user, final String input) {
        if (isStartCommand(input)) return processStartCommand(user);
        if (isNewQueryCommand(input)) return processNewQueryCommand(user);
        if (isMyQueriesCommand(input)) return processMyQueriesCommand(user);
        return "";
    }

    private boolean isStartCommand(String input) {
        return START_COMMAND.equals(input);
    }

    private boolean isNewQueryCommand(String input) {
        return NEWQUERY_COMMAND.equals(input);
    }

    private boolean isMyQueriesCommand(String input) {
        return MYQUERIES_COMMAND.equals(input);
    }

    private String processStartCommand(final AppUser user) {
        var firstName = user.getFirstName();
        return String.format(WELCOME_MESSAGE, firstName);
    }

    private String processNewQueryCommand(final AppUser user) {
        var userState = userService.getUserState(user.getTelegramId());
        if (!WAIT_CONFIG_NAME_STATE.equals(userState)) {
            userService.saveUserState(user.getTelegramId(), WAIT_CONFIG_NAME_STATE);
            log.debug("User {} changed state to WAIT_CONFIG_NAME_STATE", user.getFirstName());
        }

        return QUERY_PROMPT_MESSAGE;
    }

    protected String processMyQueriesCommand(final AppUser user) {
        var userConfigs = configService.getByUser(user);
        if (userConfigs == null || userConfigs.isEmpty()) return NO_SAVED_QUERIES_MESSAGE;

        var userState = userService.getUserState(user.getTelegramId());
        if (!QUERY_LIST_STATE.equals(userState)) {
            userService.saveUserState(user.getTelegramId(), QUERY_LIST_STATE);
            log.debug("User {} changed state to QUERY_LIST_STATE", user.getFirstName());
        }

        return USER_QUERIES_MESSAGE;
    }
}
