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
import static com.bipbup.utils.CommandMessageConstants.BotCommand.MYQUERIES;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.NEWQUERY;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.START;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.NO_SAVED_QUERIES;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.QUERY_PROMPT;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.USER_QUERIES;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.WELCOME;

@Slf4j
@RequiredArgsConstructor
@Component
public class BasicStateHandler implements StateHandler {

    private final UserService userService;

    private final ConfigService configService;

    @Override
    public String process(final AppUser user, final String input) {
        if (isStartCommand(input))
            return processStartCommand(user);
        if (isNewQueryCommand(input))
            return processNewQueryCommand(user);
        if (isMyQueriesCommand(input))
            return processMyQueriesCommand(user);
        return "";
    }

    private boolean isStartCommand(final String input) {
        return START.getCommand().equals(input);
    }

    private boolean isNewQueryCommand(final String input) {
        return NEWQUERY.getCommand().equals(input);
    }

    private boolean isMyQueriesCommand(final String input) {
        return MYQUERIES.getCommand().equals(input);
    }

    private String processStartCommand(final AppUser user) {
        return String.format(WELCOME.getTemplate(), user.getFirstName());
    }

    private String processNewQueryCommand(final AppUser user) {
        userService.saveUserState(user.getTelegramId(), WAIT_CONFIG_NAME_STATE);
        log.info("State of user {} set to WAIT_CONFIG_NAME_STATE", user.getFirstName());
        return QUERY_PROMPT.getTemplate();
    }

    protected String processMyQueriesCommand(final AppUser user) {
        var configs = configService.getByUser(user);
        if (configs == null || configs.isEmpty()) {
            userService.clearUserState(user.getTelegramId());
            return NO_SAVED_QUERIES.getTemplate();
        }

        userService.saveUserState(user.getTelegramId(), QUERY_LIST_STATE);
        log.info("State of user {} set to QUERY_LIST_STATE", user.getFirstName());
        return USER_QUERIES.getTemplate();
    }
}
