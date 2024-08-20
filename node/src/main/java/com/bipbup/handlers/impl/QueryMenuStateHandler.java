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
import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_FOUND_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.DELETE_CONFIRMATION_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.DELETE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.MYQUERIES_COMMAND;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_PREFIX;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryMenuStateHandler implements StateHandler {

    private final UserService userService;

    private final ConfigService configService;

    private final Decoder decoder;

    private final BasicStateHandler basicStateHandler;

    @Override
    public String process(AppUser user, String input) {
        if (isBackToQueryListCommand(input))
            return processBackToQueryListCommand(user);
        if (hasDeletePrefix(input))
            return processConfigActionCommand(user, input, QUERY_DELETE_STATE);
        if (hasUpdatePrefix(input))
            return processConfigActionCommand(user, input, QUERY_UPDATE_STATE);

        return "";
    }

    private String processBackToQueryListCommand(AppUser user) {
        return basicStateHandler.process(user, MYQUERIES_COMMAND);
    }

    private boolean isBackToQueryListCommand(String input) {
        return MYQUERIES_COMMAND.equals(input);
    }

    private boolean hasUpdatePrefix(String input) {
        return input.startsWith(UPDATE_PREFIX);
    }

    private boolean hasDeletePrefix(String input) {
        return input.startsWith(DELETE_PREFIX);
    }

    private void appendEnumParams(StringBuilder output, EnumParam[] values, String prefix) {
        if (values != null && values.length > 0) {
            output.append(prefix);

            String paramNames = Arrays.stream(values)
                    .map(EnumParam::getDescription)
                    .collect(Collectors.joining(", "));

            output.append(paramNames);
        }
    }

    //TODO: сделать по красоте
    private String showDetailedQueryOutput(AppUserConfig config) {
        StringBuilder output = new StringBuilder()
                .append(config.getConfigName())
                .append("\nТекст запроса: ").append(config.getQueryText())
                .append("\nРегион: ").append(config.getArea() == null ? "Любой" : config.getArea())
                .append("\nОпыт работы: ").append(config.getExperience().getDescription());

        appendEnumParams(output, config.getEducationLevels(), "\nУровень образования: ");
        appendEnumParams(output, config.getScheduleTypes(), "\nТип графика: ");

        return output.toString();
    }

    private String processConfigActionCommand(AppUser user, String input, AppUserState state) {
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getById(configId);

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            userService.saveUserState(user.getTelegramId(), state);
            log.info("User {} selected menu action and state set to {}", user.getFirstName(), state);

            if (state == QUERY_UPDATE_STATE)
                return showDetailedQueryOutput(config);

            return DELETE_CONFIRMATION_MESSAGE;
        } else {
            log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND_MESSAGE;
        }
    }
}
