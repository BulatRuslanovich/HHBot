package com.bipbup.handlers.impl.message;

import com.bipbup.annotation.MessageQualifier;
import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.QUERY_LIST_STATE;
import static com.bipbup.enums.AppUserState.WAIT_BROADCAST_MESSAGE;
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import com.bipbup.enums.Role;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.db.ConfigService;
import com.bipbup.service.bot.NotifierService;
import com.bipbup.service.cache.UserStateCacheService;
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
import com.bipbup.utils.CommandMessageConstants.MessageTemplate;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.NO_SAVED_QUERIES;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.QUERY_PROMPT;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.USER_QUERIES;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.WELCOME;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@MessageQualifier
@RequiredArgsConstructor
public class BasicStateHandler implements StateHandler {

    public static final Marker ADMIN_LOG = MarkerFactory.getMarker("ADMIN");

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
        return input.split(" ", 2)[0].equals(SEARCH.getCommand());
    }

    private boolean isBroadcastCommand(String input) {
        return input.split(" ", 2)[0].equals(BROADCAST.getCommand());
    }

    private boolean isStartCommand(String input) {
        return START.getCommand().equals(input);
    }

    private boolean isHelpCommand(String input) {
        return HELP.getCommand().equals(input);
    }

    private boolean isNewQueryCommand(String input) {
        return NEWQUERY.getCommand().equals(input);
    }

    private boolean isMyQueriesCommand(String input) {
        return MYQUERIES.getCommand().equals(input);
    }

    private String processStartCommand(AppUser user) {
        return String.format(WELCOME.getTemplate(), user.getFirstName());
    }

    private String processHelpCommand() {
        return MessageTemplate.HELP.getTemplate();
    }

    private String processNewQueryCommand(AppUser user) {
        userStateCacheService.putUserState(user.getTelegramId(), WAIT_CONFIG_NAME_STATE);
        log.info("State of user {} set to WAIT_CONFIG_NAME_STATE", user.getFirstName());
        return QUERY_PROMPT.getTemplate();
    }

    private String processMyQueriesCommand(AppUser user) {
        var configs = configService.getConfigByUser(user);
        if (configs == null || configs.isEmpty()) {
            userStateCacheService.clearUserState(user.getTelegramId());
            return NO_SAVED_QUERIES.getTemplate();
        }

        userStateCacheService.putUserState(user.getTelegramId(), QUERY_LIST_STATE);
        log.info("State of user {} set to QUERY_LIST_STATE", user.getFirstName());
        return USER_QUERIES.getTemplate();
    }

    private String processBroadcastCommand(AppUser user, String input) {
        return processAdminCommand(user, input, BROADCAST.getCommand(), () -> {
            userStateCacheService.putUserState(user.getTelegramId(), WAIT_BROADCAST_MESSAGE);
            return ENTER_MESSAGE.getTemplate();
        });
    }

    private String processSearchCommand(AppUser user, String input) {
        return processAdminCommand(user, input, SEARCH.getCommand(), () -> {
            log.info(ADMIN_LOG, "{} launched vacancies searching", user.getFirstName());
            notifierService.searchNewVacancies();
            return SEARCHING_COMPLETED.getTemplate();
        });
    }

    private String processAdminCommand(AppUser user, String input, String command, Supplier<String> action) {
        if (!user.getRole().equals(Role.ADMIN)) {
            return NO_PERMISSION.getTemplate();
        }

        var split = input.split(" ", 2);

        if (split.length != 2) {
            return USAGE.getTemplate().formatted(command);
        }

        var password = split[1];

        if (adminPassword.equals(password)) {
            return action.get();
        }

        return INCORRECT_PASSWORD.getTemplate();
    }
}
