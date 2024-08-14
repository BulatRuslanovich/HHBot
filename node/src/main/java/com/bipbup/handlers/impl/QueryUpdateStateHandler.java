package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.WAIT_AREA_STATE;
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import static com.bipbup.enums.AppUserState.WAIT_EXPERIENCE_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;
import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_FOUND_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.ENTER_AREA_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.ENTER_CONFIG_NAME_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.ENTER_QUERY_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.QUERY_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.SELECT_EXPERIENCE_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_AREA_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_CONFIG_NAME_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_EXPERIENCE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_QUERY_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_STATE_PREFIX;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryUpdateStateHandler implements StateHandler {
    private final UserService userService;

    private final ConfigService configService;

    private final Decoder decoder;

    private final QueryListStateHandler queryListStateHandler;

    @Override
    public String process(AppUser user, String input) {
        if (isBackToQueryMenuCommand(input))
            return processBackToQueryMenuCommand(user, input);
        if (hasUpdatePrefix(input))
            return processUpdateConfigCommand(user, input);

        return "";
    }

    private boolean hasUpdatePrefix(String input) {
        return input.startsWith(UPDATE_STATE_PREFIX);
    }

    private String processBackToQueryMenuCommand(AppUser user, String input) {
        return queryListStateHandler.process(user, input);
    }

    private boolean isBackToQueryMenuCommand(String input) {
        return input.startsWith(QUERY_PREFIX);
    }

    private String processUpdateConfigCommand(AppUser user, String input) {
        if (hasUpdateConfigNamePrefix(input))
            return updateConfigSelectionAndUserState(user, input,
                    UPDATE_CONFIG_NAME_PREFIX,
                    WAIT_CONFIG_NAME_STATE,
                    ENTER_CONFIG_NAME_MESSAGE_TEMPLATE,
                    true);
        if (hasUpdateQueryPrefix(input))
            return updateConfigSelectionAndUserState(user, input,
                    UPDATE_QUERY_PREFIX,
                    WAIT_QUERY_STATE,
                    ENTER_QUERY_MESSAGE_TEMPLATE,
                    true);
        if (hasUpdateExperiencePrefix(input))
            return updateConfigSelectionAndUserState(user, input,
                    UPDATE_EXPERIENCE_PREFIX,
                    WAIT_EXPERIENCE_STATE,
                    SELECT_EXPERIENCE_MESSAGE_TEMPLATE,
                    false);
        if (hasUpdateAreaPrefix(input))
            return updateConfigSelectionAndUserState(user, input,
                    UPDATE_AREA_PREFIX,
                    WAIT_AREA_STATE,
                    ENTER_AREA_MESSAGE_TEMPLATE,
                    true);

        return "";
    }

    private boolean hasUpdateQueryPrefix(String input) {
        return input.startsWith(UPDATE_QUERY_PREFIX);
    }

    private boolean hasUpdateConfigNamePrefix(String input) {
        return input.startsWith(UPDATE_CONFIG_NAME_PREFIX);
    }

    private boolean hasUpdateExperiencePrefix(String input) {
        return input.startsWith(UPDATE_EXPERIENCE_PREFIX);
    }

    private boolean hasUpdateAreaPrefix(String input) {
        return input.startsWith(UPDATE_AREA_PREFIX);
    }

    private String updateConfigSelectionAndUserState(AppUser user,
                                                     String input,
                                                     String prefix,
                                                     AppUserState state,
                                                     String messageTemplate,
                                                     boolean shouldSaveConfigSelection) {
        var hash = input.substring(prefix.length());
        var configId = decoder.idOf(hash);
        var configOptional = configService.getById(configId);

        if (configOptional.isPresent()) {
            var configName = configOptional.get().getConfigName();

            userService.saveUserState(user.getTelegramId(), state);
            if (shouldSaveConfigSelection)
                configService.saveConfigSelection(user.getTelegramId(), configId);

            log.debug("User {} selected parameter to edit and state set to {}", user.getFirstName(), state);
            return String.format(messageTemplate, configName);
        } else {
            userService.clearUserState(user.getTelegramId());
            log.warn("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND_MESSAGE;
        }
    }
}
