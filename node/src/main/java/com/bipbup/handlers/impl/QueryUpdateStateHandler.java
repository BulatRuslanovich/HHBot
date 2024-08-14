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
import static com.bipbup.enums.AppUserState.WAIT_EXPERIENCE_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;
import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_FOUND_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.ENTER_CONFIG_NAME_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.ENTER_QUERY_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.QUERY_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.SELECT_EXPERIENCE_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_CONFIG_NAME_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_EXPERIENCE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_QUERY_PREFIX;

@Slf4j
@Component
public class QueryUpdateStateHandler implements StateHandler {
    private final UserService userService;

    private final ConfigService configService;

    private final Decoder decoder;

    private final QueryListStateHandler queryListStateHandler;

    public QueryUpdateStateHandler(UserService userService,
                                   Decoder decoder,
                                   ConfigService configService,
                                   QueryListStateHandler queryListStateHandler) {
        this.userService = userService;
        this.decoder = decoder;
        this.configService = configService;
        this.queryListStateHandler = queryListStateHandler;
    }

    @Override
    public String process(AppUser user, String input) {
        if (isBackToQueryMenuCommand(input))
            return processBackToQueryMenuCommand(user, input);
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
        if (hasEditExperiencePrefix(input))
            return processEditConfigCommand(user, input,
                    UPDATE_EXPERIENCE_PREFIX.length(),
                    WAIT_EXPERIENCE_STATE,
                    SELECT_EXPERIENCE_MESSAGE);
        
        return "";
    }

    private String processBackToQueryMenuCommand(AppUser user, String input) {
        return queryListStateHandler.process(user, input);
    }

    private boolean isBackToQueryMenuCommand(String input) {
        return input.startsWith(QUERY_PREFIX);
    }

    private boolean hasEditQueryPrefix(String input) {
        return input.startsWith(UPDATE_QUERY_PREFIX);
    }

    private boolean hasEditConfigNamePrefix(String input) {
        return input.startsWith(UPDATE_CONFIG_NAME_PREFIX);
    }

    private boolean hasEditExperiencePrefix(String input) {
        return input.startsWith(UPDATE_EXPERIENCE_PREFIX);
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
