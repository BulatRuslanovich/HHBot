package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.AppUserState;
import com.bipbup.enums.EnumParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.bipbup.enums.AppUserState.QUERY_DELETE_STATE;
import static com.bipbup.enums.AppUserState.QUERY_UPDATE_STATE;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.MYQUERIES;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.DELETE_CONFIRMATION;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.MENU_AREA;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.MENU_CONFIG_NAME;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.MENU_EDUCATION;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.MENU_EXPERIENCE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.MENU_QUERY;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.MENU_SCHEDULE;
import static com.bipbup.utils.CommandMessageConstants.Prefix;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryMenuStateHandler implements StateHandler {

    private final UserService userService;

    private final ConfigService configService;

    private final Decoder decoder;

    private final BasicStateHandler basicStateHandler;

    @Override
    public String process(final AppUser user, final String input) {
        if (isBackToQueryListCommand(input))
            return processBackToQueryListCommand(user);
        if (hasDeletePrefix(input))
            return processConfigActionCommand(user, input, QUERY_DELETE_STATE);
        if (hasUpdatePrefix(input))
            return processConfigActionCommand(user, input, QUERY_UPDATE_STATE);

        return "";
    }

    private String processBackToQueryListCommand(final AppUser user) {
        return basicStateHandler.process(user, MYQUERIES.getCommand());
    }

    private boolean isBackToQueryListCommand(final String input) {
        return MYQUERIES.getCommand().equals(input);
    }

    private boolean hasUpdatePrefix(final String input) {
        return input.startsWith(Prefix.UPDATE);
    }

    private boolean hasDeletePrefix(final String input) {
        return input.startsWith(Prefix.DELETE);
    }

    private void appendEnumParams(StringBuilder output, final EnumParam[] values, final String prefix) {
        if (values != null && values.length > 0) {
            output.append('\n').append(prefix).append('\n');

            String paramNames = Arrays.stream(values)
                    .map(param -> "  - " + param.getDescription())
                    .collect(Collectors.joining("\n"));

            output.append(paramNames);
        }
    }

    private String showDetailedQueryOutput(final AppUserConfig config) {
        StringBuilder output = new StringBuilder()
                .append(MENU_CONFIG_NAME.getTemplate()).append(config.getConfigName()).append("\n")
                .append(MENU_QUERY.getTemplate()).append(config.getQueryText()).append("\n")
                .append(MENU_AREA.getTemplate()).append(config.getArea() == null ? "Любой" : config.getArea()).append("\n")
                .append(MENU_EXPERIENCE.getTemplate()).append(config.getExperience().getDescription());

        appendEnumParams(output, config.getEducationLevels(), MENU_EDUCATION.getTemplate());
        appendEnumParams(output, config.getScheduleTypes(), MENU_SCHEDULE.getTemplate());

        return output.toString();
    }

    private String processConfigActionCommand(final AppUser user, final String input, final AppUserState state) {
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getById(configId);

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            userService.saveUserState(user.getTelegramId(), state);
            log.info("User {} selected menu action and state set to {}", user.getFirstName(), state);

            if (state == QUERY_UPDATE_STATE)
                return showDetailedQueryOutput(config);

            return String.format(DELETE_CONFIRMATION.getTemplate(), config.getConfigName());
        } else {
            log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND.getTemplate();
        }
    }
}
