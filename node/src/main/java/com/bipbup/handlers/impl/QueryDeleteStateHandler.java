package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.utils.CommandMessageConstants.CONFIG_DELETED_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_DELETED_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_FOUND_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.DELETE_CANCEL_COMMAND;
import static com.bipbup.utils.CommandMessageConstants.DELETE_CONFIRM_PREFIX;

@Slf4j
@Component
public class QueryDeleteStateHandler implements StateHandler {
    private final UserService userService;

    private final ConfigService configService;

    private final Decoder decoder;

    public QueryDeleteStateHandler(UserService userService,
                                   ConfigService configService,
                                   Decoder decoder) {
        this.userService = userService;
        this.configService = configService;
        this.decoder = decoder;
    }

    @Override
    public String process(AppUser user, String input) {
        if (hasDeleteYesPrefix(input)) return processDeleteYesCommand(user, input);
        if (isDeleteNoCommand(input)) return processDeleteNoCommand(user);

        return "";
    }

    private boolean isDeleteNoCommand(final String input) {
        return DELETE_CANCEL_COMMAND.equals(input);
    }

    private boolean hasDeleteYesPrefix(final String input) {
        return input.startsWith(DELETE_CONFIRM_PREFIX);
    }

    private String processDeleteYesCommand(AppUser user, String input) {
        var hash = input.substring(DELETE_CONFIRM_PREFIX.length());
        var configId = decoder.idOf(hash);
        var optional = configService.getById(configId);

        if (optional.isPresent()) {
            configService.delete(optional.get());
            userService.clearUserState(user.getTelegramId());
            log.debug("User {} deleted configuration with id {} and state set to BASIC_STATE", user.getFirstName(), configId);
            return CONFIG_DELETED_MESSAGE;
        } else {
            log.warn("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND_MESSAGE;
        }
    }

    private String processDeleteNoCommand(AppUser user) {
        userService.clearUserState(user.getTelegramId());
        log.debug("User {} chose not to delete the configuration and state set to BASIC_STATE", user.getFirstName());
        return CONFIG_NOT_DELETED_MESSAGE;
    }
}
