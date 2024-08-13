package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;
import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_FOUND_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.ENTER_CONFIG_NAME_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.ENTER_QUERY_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_CONFIG_NAME_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_QUERY_PREFIX;

@Slf4j
@Component
public class QueryUpdateStateHandler implements StateHandler {
    private final UserService userService;

    private final ConfigService configService;

    private final Decoder decoder;

    public QueryUpdateStateHandler(UserService userService,
                                   Decoder decoder,
                                   ConfigService configService) {
        this.userService = userService;
        this.decoder = decoder;
        this.configService = configService;
    }

    @Override
    public String process(AppUser user, String input) {
        if (hasEditConfigNamePrefix(input))
            return processEditConfigCommand(user, input,
                    UPDATE_CONFIG_NAME_PREFIX.length(),
                    WAIT_CONFIG_NAME_STATE,
                    ENTER_CONFIG_NAME_MESSAGE);
        if (hasEditQueryPrefix(input))
            return processEditConfigCommand(user, input,
                    UPDATE_QUERY_PREFIX.length(),
                    WAIT_QUERY_STATE,
                    ENTER_QUERY_MESSAGE);
        
        return "";
    }
    
    private boolean hasEditQueryPrefix(String input) {
        return input.startsWith(UPDATE_QUERY_PREFIX);
    }

    private boolean hasEditConfigNamePrefix(String input) {
        return input.startsWith(UPDATE_CONFIG_NAME_PREFIX);
    }

    private String processEditConfigCommand(AppUser user,
                                            String input,
                                            int prefixLength,
                                            AppUserState state,
                                            String message) {
        var hash = input.substring(prefixLength);
        var configId = decoder.idOf(hash);
        var optional = configService.getById(configId);

        if (optional.isPresent()) {
            userService.saveUserState(user.getTelegramId(), state);
            configService.saveConfigSelection(user.getTelegramId(), configId);
            log.debug("User {} selected parameter to edit and state set to {}", user.getFirstName(), state);
            return message;
        } else {
            log.warn("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND_MESSAGE;
        }
    }
}
