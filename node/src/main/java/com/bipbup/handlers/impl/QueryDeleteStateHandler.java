package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.utils.CommandMessageConstants.CONFIG_DELETED_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_DELETED_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_FOUND_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.DELETE_CANCEL_COMMAND;
import static com.bipbup.utils.CommandMessageConstants.DELETE_CONFIRM_PREFIX;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryDeleteStateHandler implements StateHandler {

    private final UserService userService;

    private final ConfigService configService;

    private final Decoder decoder;

    @Override
    public String process(AppUser user, String input) {
        if (hasDeleteConfirmPrefix(input))
            return processDeleteConfirmCommand(user, input);
        if (isDeleteCancelCommand(input))
            return processDeleteCancelCommand(user);

        return "";
    }

    private boolean isDeleteCancelCommand(final String input) {
        return DELETE_CANCEL_COMMAND.equals(input);
    }

    private boolean hasDeleteConfirmPrefix(final String input) {
        return input.startsWith(DELETE_CONFIRM_PREFIX);
    }

    private String processDeleteConfirmCommand(AppUser user, String input) {
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getById(configId);

        userService.clearUserState(user.getTelegramId());

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            configService.delete(config);
            log.info("User {} deleted configuration with id {} and state set to BASIC_STATE", user.getFirstName(), configId);
            return CONFIG_DELETED_MESSAGE;
        } else {
            log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND_MESSAGE;
        }
    }

    private String processDeleteCancelCommand(AppUser user) {
        userService.clearUserState(user.getTelegramId());
        log.info("User {} chose not to delete the configuration and state set to BASIC_STATE", user.getFirstName());
        return CONFIG_NOT_DELETED_MESSAGE;
    }
}
