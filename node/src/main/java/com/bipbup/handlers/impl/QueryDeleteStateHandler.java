package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.QUERY_LIST_STATE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_DELETED;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.Prefix;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryDeleteStateHandler implements StateHandler {

    private final UserService userService;

    private final ConfigService configService;

    private final QueryListStateHandler queryListStateHandler;

    private final Decoder decoder;

    @Override
    public String process(final AppUser user, final String input) {
        if (hasDeleteConfirmPrefix(input))
            return processDeleteConfirmCommand(user, input);
        if (isDeleteCancelCommand(input))
            return processDeleteCancelCommand(user, input);

        return "";
    }

    private boolean isDeleteCancelCommand(final String input) {
        return input.startsWith(Prefix.QUERY);
    }

    private boolean hasDeleteConfirmPrefix(final String input) {
        return input.startsWith(Prefix.DELETE_CONFIRM);
    }

    private String processDeleteConfirmCommand(final AppUser user, final String input) {
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getById(configId);

        userService.clearUserState(user.getTelegramId());

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            configService.delete(config);
            log.info("User {} deleted configuration with id {} and state set to BASIC_STATE", user.getFirstName(), configId);
            return String.format(CONFIG_DELETED.getTemplate(), config.getConfigName());
        } else {
            log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND.getTemplate();
        }
    }

    private String processDeleteCancelCommand(final AppUser user, final String input) {
        userService.saveUserState(user.getTelegramId(), QUERY_LIST_STATE);
        log.info("User {} chose not to delete the configuration and state set to QUERY_LIST_STATE", user.getFirstName());
        return queryListStateHandler.process(user, input);
    }
}
