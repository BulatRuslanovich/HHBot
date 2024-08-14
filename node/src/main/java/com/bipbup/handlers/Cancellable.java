package com.bipbup.handlers;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.impl.BasicStateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.bipbup.utils.CommandMessageConstants.CANCEL_COMMAND;
import static com.bipbup.utils.CommandMessageConstants.COMMAND_CANCELLED_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_FOUND_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.INVALID_INPUT_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.MYQUERIES_COMMAND;
import static com.bipbup.utils.CommandMessageConstants.NEWQUERY_COMMAND;

@Slf4j
@RequiredArgsConstructor
public abstract class Cancellable {

    protected final UserService userService;

    protected final ConfigService configService;

    private final BasicStateHandler basicStateHandler;

    protected boolean isCancelCommand(final String input) {
        return CANCEL_COMMAND.equals(input);
    }

    protected boolean isBasicCommand(String input) {
        return MYQUERIES_COMMAND.equals(input)
                || NEWQUERY_COMMAND.equals(input);
    }

    protected String processCancelCommand(final AppUser user) {
        userService.clearUserState(user.getTelegramId());
        log.debug("User {} cancelled the command and state set to BASIC_STATE.", user.getFirstName());
        return COMMAND_CANCELLED_MESSAGE;
    }

    protected String processBasicCommand(AppUser user, String input) {
        return basicStateHandler.process(user, input);
    }

    protected String processInvalidInput(final AppUser user) {
        userService.clearUserState(user.getTelegramId());
        log.debug("User {} provided an invalid input and state set to BASIC_STATE.", user.getFirstName());
        return INVALID_INPUT_MESSAGE;
    }

    protected String processConfigNotFoundMessage(final AppUser user) {
        var configId = configService.getSelectedConfigId(user.getTelegramId());
        configService.clearConfigSelection(user.getTelegramId());
        userService.clearUserState(user.getTelegramId());
        log.warn("Config with id {} not found for user {}", configId, user.getTelegramId());
        return CONFIG_NOT_FOUND_MESSAGE;
    }

    protected AppUserConfig fetchConfig(final AppUser user) {
        if (!isConfigUpdating(user)) {
            return fetchLastConfig(user);
        }

        var configId = configService.getSelectedConfigId(user.getTelegramId());
        var optionalConfig = configService.getById(configId);

        if (optionalConfig.isPresent()) {
            configService.clearConfigSelection(user.getTelegramId());
            return optionalConfig.get();
        }

        return null;
    }

    protected AppUserConfig fetchLastConfig(final AppUser user) {
        List<AppUserConfig> configs = configService.getByUser(user);

        if (configs.isEmpty()) {
            log.error("No configurations found for user.");
            return null;
        }

        return configs.get(configs.size() - 1);
    }

    protected boolean isConfigUpdating(final AppUser user) {
        Long configId = configService.getSelectedConfigId(user.getTelegramId());
        return configId != null;
    }
}
