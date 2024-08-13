package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.EnumParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.QUERY_DELETE_STATE;
import static com.bipbup.enums.AppUserState.QUERY_UPDATE_STATE;
import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_FOUND_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.DELETE_CONFIRMATION_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.DELETE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_PREFIX;

@Slf4j
@Component
public class QueryMenuStateHandler implements StateHandler {
    private final UserService userService;

    private final ConfigService configService;

    private final Decoder decoder;

    public QueryMenuStateHandler(UserService userService,
                                 ConfigService configService,
                                 Decoder decoder) {
        this.userService = userService;
        this.configService = configService;
        this.decoder = decoder;
    }

    @Override
    public String process(AppUser user, String input) {
        if (hasDeletePrefix(input)) return processDeleteCommand(user);
        if (hasUpdatePrefix(input)) return processUpdateCommand(user, input);
        
        return "";
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
                .append("\nРегион: ").append(config.getRegion() == null ? "Любой" : config.getRegion())
                .append("\nОпыт работы: ").append(config.getExperience().getDescription());

        appendEnumParams(output, config.getEducationLevels(), "\nУровень образования: ");
        appendEnumParams(output, config.getScheduleTypes(), "\nТип графика: ");

        return output.toString();
    }

    private String processDeleteCommand(AppUser user) {
        userService.saveUserState(user.getTelegramId(), QUERY_DELETE_STATE);
        log.debug("User {} set state to QUERY_DELETE_STATE", user.getFirstName());
        return DELETE_CONFIRMATION_MESSAGE;
    }

    private String processUpdateCommand(AppUser user, String input) {
        var hash = input.substring(UPDATE_PREFIX.length());
        var configId = decoder.idOf(hash);
        var optionalAppUserConfig = configService.getById(configId);

        if (optionalAppUserConfig.isPresent()) {
            AppUserConfig config = optionalAppUserConfig.get();

            userService.saveUserState(user.getTelegramId(), QUERY_UPDATE_STATE);
            log.debug("User {} set state to QUERY_UPDATE_STATE", user.getFirstName());

            return showDetailedQueryOutput(config);
        } else {
            log.warn("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND_MESSAGE;
        }
    }
}
