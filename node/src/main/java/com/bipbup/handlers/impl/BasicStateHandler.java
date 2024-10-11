package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.Role;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.NotifierService;
import com.bipbup.service.UserService;
import com.bipbup.utils.CommandMessageConstants.MessageTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static com.bipbup.enums.AppUserState.QUERY_LIST_STATE;
import static com.bipbup.enums.AppUserState.WAIT_BROADCAST_MESSAGE;
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import static com.bipbup.utils.CommandMessageConstants.AdminCommand.BROADCAST;
import static com.bipbup.utils.CommandMessageConstants.AdminCommand.SEARCH;
import static com.bipbup.utils.CommandMessageConstants.AdminMessageTemplate.*;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.HELP;
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

    protected static final Marker ADMIN_LOG = MarkerFactory.getMarker("ADMIN");

    private final UserService userService;

    private final ConfigService configService;

    private final NotifierService notifierService;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public String process(final AppUser user, final String input) {
        userService.clearUserState(user.getTelegramId());

        if (isStartCommand(input))
            return processStartCommand(user);
        if (isHelpCommand(input))
            return processHelpCommand();
        if (isNewQueryCommand(input))
            return processNewQueryCommand(user);
        if (isMyQueriesCommand(input))
            return processMyQueriesCommand(user);
        if (isBroadcastCommand(input))
            return processBroadcastCommand(user, input);
        if (isSearchCommand(input))
            return processSearchCommand(user, input);

        return "";
    }

    private boolean isSearchCommand(final String input) {
        return input.split(" ", 2)[0].equals(SEARCH.getCommand());
    }

    private boolean isBroadcastCommand(String input) {
        return input.split(" ", 2)[0].equals(BROADCAST.getCommand());
    }

    private boolean isStartCommand(final String input) {
        return START.getCommand().equals(input);
    }

    private boolean isHelpCommand(final String input) {
        return HELP.getCommand().equals(input);
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

    private String processHelpCommand() {
        return MessageTemplate.HELP.getTemplate();
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

    private String processBroadcastCommand(final AppUser user, final String input) {
        return processAdminCommand(user, input, BROADCAST.getCommand(), () -> {
            userService.saveUserState(user.getTelegramId(), WAIT_BROADCAST_MESSAGE);
            return ENTER_MESSAGE.getTemplate();
        });
    }

    private String processSearchCommand(final AppUser user, final String input) {
        return processAdminCommand(user, input, SEARCH.getCommand(), () -> {
            log.info(ADMIN_LOG, "{} launched vacancies searching", user.getFirstName());
            notifierService.searchNewVacancies();
            return SEARCHING_COMPLETED.getTemplate();
        });
    }

    private String processAdminCommand(final AppUser user,
                                       final String input,
                                       final String command,
                                       final Supplier<String> action) {
        if (!user.getRole().equals(Role.ADMIN))
            return NO_PERMISSION.getTemplate();

        var split = input.split(" ", 2);

        if (split.length != 2)
            return USAGE.getTemplate().formatted(command);

        var password = split[1];

        if (adminPassword.equals(password))
            return action.get();

        return INCORRECT_PASSWORD.getTemplate();
    }
}
