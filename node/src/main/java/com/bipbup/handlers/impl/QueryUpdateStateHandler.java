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

import java.util.Map;

import static com.bipbup.enums.AppUserState.WAIT_AREA_STATE;
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import static com.bipbup.enums.AppUserState.WAIT_EDUCATION_STATE;
import static com.bipbup.enums.AppUserState.WAIT_EXPERIENCE_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;
import static com.bipbup.enums.AppUserState.WAIT_SCHEDULE_STATE;
import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_FOUND_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.ENTER_AREA_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.ENTER_CONFIG_NAME_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.ENTER_QUERY_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.QUERY_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.SELECT_EDUCATION_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.SELECT_EXPERIENCE_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.SELECT_SCHEDULE_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_AREA_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_CONFIG_NAME_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_EDUCATION_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_EXPERIENCE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_QUERY_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_SCHEDULE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_STATE_PREFIX;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryUpdateStateHandler implements StateHandler {

    private final UserService userService;

    private final ConfigService configService;

    private final Decoder decoder;

    private final QueryListStateHandler queryListStateHandler;

    private static final Map<String, ActionProperties> actionPropertiesMap = Map.of(
            UPDATE_CONFIG_NAME_PREFIX, new ActionProperties(WAIT_CONFIG_NAME_STATE, ENTER_CONFIG_NAME_MESSAGE_TEMPLATE, true),
            UPDATE_QUERY_PREFIX, new ActionProperties(WAIT_QUERY_STATE, ENTER_QUERY_MESSAGE_TEMPLATE, true),
            UPDATE_EXPERIENCE_PREFIX, new ActionProperties(WAIT_EXPERIENCE_STATE, SELECT_EXPERIENCE_MESSAGE_TEMPLATE, false),
            UPDATE_AREA_PREFIX, new ActionProperties(WAIT_AREA_STATE, ENTER_AREA_MESSAGE_TEMPLATE, true),
            UPDATE_EDUCATION_PREFIX, new ActionProperties(WAIT_EDUCATION_STATE, SELECT_EDUCATION_MESSAGE_TEMPLATE, false),
            UPDATE_SCHEDULE_PREFIX, new ActionProperties(WAIT_SCHEDULE_STATE, SELECT_SCHEDULE_MESSAGE_TEMPLATE, false)
    );

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
        var prefix = input.substring(0, input.lastIndexOf('_') + 1);
        var properties = actionPropertiesMap.get(prefix);

        return updateConfigSelectionAndUserState(user, input, properties);
    }

    private String updateConfigSelectionAndUserState(AppUser user,
                                                     String input,
                                                     ActionProperties properties) {
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getById(configId);

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            userService.saveUserState(user.getTelegramId(), properties.state());

            if (properties.saveSelection())
                configService.saveConfigSelection(user.getTelegramId(), configId);

            log.info("User {} selected parameter to edit and state set to {}", user.getFirstName(), properties.state());
            return String.format(properties.output(), config.getConfigName());
        } else {
            userService.clearUserState(user.getTelegramId());
            log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND_MESSAGE;
        }
    }
}

record ActionProperties(AppUserState state, String output, boolean saveSelection) {}
