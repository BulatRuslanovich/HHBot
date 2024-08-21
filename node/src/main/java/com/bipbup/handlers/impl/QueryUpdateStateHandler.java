package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
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
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.ENTER_AREA;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.ENTER_CONFIG_NAME;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.ENTER_QUERY;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.SELECT_EDUCATION;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.SELECT_EXPERIENCE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.SELECT_SCHEDULE;
import static com.bipbup.utils.CommandMessageConstants.Prefix;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryUpdateStateHandler implements StateHandler {

    private final UserService userService;

    private final ConfigService configService;

    private final Decoder decoder;

    private final QueryListStateHandler queryListStateHandler;

    private static final Map<String, ActionProperties> actionPropertiesMap = Map.of(
            Prefix.UPDATE_CONFIG_NAME, new ActionProperties(WAIT_CONFIG_NAME_STATE, ENTER_CONFIG_NAME.getTemplate(), true),
            Prefix.UPDATE_QUERY, new ActionProperties(WAIT_QUERY_STATE, ENTER_QUERY.getTemplate(), true),
            Prefix.UPDATE_EXPERIENCE, new ActionProperties(WAIT_EXPERIENCE_STATE, SELECT_EXPERIENCE.getTemplate(), false),
            Prefix.UPDATE_AREA, new ActionProperties(WAIT_AREA_STATE, ENTER_AREA.getTemplate(), true),
            Prefix.UPDATE_EDUCATION, new ActionProperties(WAIT_EDUCATION_STATE, SELECT_EDUCATION.getTemplate(), false),
            Prefix.UPDATE_SCHEDULE, new ActionProperties(WAIT_SCHEDULE_STATE, SELECT_SCHEDULE.getTemplate(), false)
    );

    @Override
    public String process(final AppUser user, final String input) {
        if (isBackToQueryMenuCommand(input))
            return processBackToQueryMenuCommand(user, input);
        if (hasUpdatePrefix(input))
            return processUpdateConfigCommand(user, input);

        return "";
    }

    private boolean hasUpdatePrefix(final String input) {
        return input.startsWith(Prefix.UPDATE_STATE);
    }

    private String processBackToQueryMenuCommand(final AppUser user, final String input) {
        return queryListStateHandler.process(user, input);
    }

    private boolean isBackToQueryMenuCommand(final String input) {
        return input.startsWith(Prefix.QUERY);
    }

    private String processUpdateConfigCommand(final AppUser user, final String input) {
        var prefix = input.substring(0, input.lastIndexOf('_') + 1);
        var properties = actionPropertiesMap.get(prefix);

        return updateConfigSelectionAndUserState(user, input, properties);
    }

    private String updateConfigSelectionAndUserState(final AppUser user,
                                                     final String input,
                                                     final ActionProperties properties) {
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
            return CONFIG_NOT_FOUND.getTemplate();
        }
    }
}