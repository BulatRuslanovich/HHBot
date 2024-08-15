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
        if (isBackToQueryListCommand(input)) return processBackToQueryListCommand(user);
        if (hasDeletePrefix(input)) return processDeleteCommand(user, input);
        if (hasUpdatePrefix(input)) return processUpdateCommand(user, input);

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
            for (EnumParam value : values) {
                output.append(value.getDescription()).append(", ");
            }
            output.setLength(output.length() - " ,".length());
        }
    }

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

    private String processDeleteCommand(AppUser user, String input) {
        return processConfigActionCommand(user, input, DELETE_PREFIX, QUERY_DELETE_STATE);
    }

    private String processUpdateCommand(AppUser user, String input) {
        return processConfigActionCommand(user, input, UPDATE_PREFIX, QUERY_UPDATE_STATE);
    }

    private String processConfigActionCommand(AppUser user, String input,
                                              String prefix, AppUserState state) {
        var hash = input.substring(prefix.length());
        var configId = decoder.idOf(hash);
        var optionalAppUserConfig = configService.getById(configId);

        if (optionalAppUserConfig.isPresent()) {
            AppUserConfig config = optionalAppUserConfig.get();

            userService.saveUserState(user.getTelegramId(), state);
            log.info("User {} selected menu action and state set to {}", user.getFirstName(), state);

            if (prefix.equals(UPDATE_PREFIX))
                return showDetailedQueryOutput(config);
            else
                return DELETE_CONFIRMATION_MESSAGE;
        } else {
            log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND_MESSAGE;
        }
    }
}
