package com.bipbup.handlers.impl.message;

import com.bipbup.annotation.MessageQualifier;
import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import com.bipbup.enums.Role;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.bot.NotifierService;
import com.bipbup.service.cache.UserStateCacheService;
import com.bipbup.service.db.ConfigService;
import com.bipbup.utils.CommandMessageConstants.MessageTemplate;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.QUERY_LIST_STATE;
import static com.bipbup.enums.AppUserState.WAIT_BROADCAST_MESSAGE;
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import static com.bipbup.utils.CommandMessageConstants.AdminCommand.BROADCAST;
import static com.bipbup.utils.CommandMessageConstants.AdminCommand.SEARCH;
import static com.bipbup.utils.CommandMessageConstants.AdminMessageTemplate.ENTER_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.AdminMessageTemplate.INCORRECT_PASSWORD;
import static com.bipbup.utils.CommandMessageConstants.AdminMessageTemplate.NO_PERMISSION;
import static com.bipbup.utils.CommandMessageConstants.AdminMessageTemplate.SEARCHING_COMPLETED;
import static com.bipbup.utils.CommandMessageConstants.AdminMessageTemplate.USAGE;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.HELP;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.MYQUERIES;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.NEWQUERY;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.START;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.NO_SAVED_QUERIES;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.QUERY_PROMPT;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.USER_QUERIES;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.WELCOME;

@Slf4j
@Component
@MessageQualifier
@RequiredArgsConstructor
public class BasicStateHandler implements StateHandler {

    private final UserStateCacheService userStateCacheService;

    private final ConfigService configService;

    private final NotifierService notifierService;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public String process(AppUser user, String input) {
        String output = "";

        if (isStartCommand(input))
            output = processStartCommand(user);
        if (isHelpCommand(input))
            output = processHelpCommand();
        if (isNewQueryCommand(input))
            output = processNewQueryCommand(user);
        if (isMyQueriesCommand(input))
            output = processMyQueriesCommand(user);
        if (isBroadcastCommand(input))
            output = processBroadcastCommand(user, input);
        if (isSearchCommand(input))
            output = processSearchCommand(user, input);
        return output;
    }

    @Override
    public AppUserState state() {
        return BASIC_STATE;
    }

    private boolean isSearchCommand(String input) {
        return input.split(" ", 2)[0].equals(SEARCH.toString());
    }

    private boolean isBroadcastCommand(String input) {
        return input.split(" ", 2)[0].equals(BROADCAST.toString());
    }

    private boolean isStartCommand(String input) {
        return input.equals(START.toString());
    }

    private boolean isHelpCommand(String input) {
        return input.equals(HELP.toString());
    }

    private boolean isNewQueryCommand(String input) {
        return input.equals(NEWQUERY.toString());
    }

    private boolean isMyQueriesCommand(String input) {
        return input.equals(MYQUERIES.toString());
    }

    private String processStartCommand(AppUser user) {
        return String.format(WELCOME.toString(), user.getFirstName());
    }

    private String processHelpCommand() {
        return MessageTemplate.HELP.toString();
    }

    private String processNewQueryCommand(AppUser user) {
        userStateCacheService.putUserState(user.getTelegramId(), WAIT_CONFIG_NAME_STATE);
        log.info("State of user {} set to WAIT_CONFIG_NAME_STATE", user.getFirstName());
        return QUERY_PROMPT.toString();
    }

    private String processMyQueriesCommand(AppUser user) {
        var configs = configService.getConfigByUser(user);
        if (configs == null || configs.isEmpty()) {
            userStateCacheService.clearUserState(user.getTelegramId());
            return NO_SAVED_QUERIES.toString();
        }

        userStateCacheService.putUserState(user.getTelegramId(), QUERY_LIST_STATE);
        log.info("State of user {} set to QUERY_LIST_STATE", user.getFirstName());
        return USER_QUERIES.toString();
    }

    private String processBroadcastCommand(AppUser user, String input) {
        return processAdminCommand(user, input, BROADCAST.toString(), () -> {
            userStateCacheService.putUserState(user.getTelegramId(), WAIT_BROADCAST_MESSAGE);
            return ENTER_MESSAGE.toString();
        });
    }

    private String processSearchCommand(AppUser user, String input) {
        return processAdminCommand(user, input, SEARCH.toString(), () -> {
            log.info("Admin: {} launched vacancies searching", user.getFirstName());
            notifierService.searchNewVacancies();
            return SEARCHING_COMPLETED.toString();
        });
    }

    private String processAdminCommand(AppUser user, String input, String command, Supplier<String> action) {
        if (!user.getRole().equals(Role.ADMIN)) {
            return NO_PERMISSION.toString();
        }

        var split = input.split(" ", 2);

        if (split.length != 2) {
            return USAGE.toString().formatted(command);
        }

        var password = split[1];

        if (adminPassword.equals(password)) {
            return action.get();
        }

        return INCORRECT_PASSWORD.toString();
    }
}
