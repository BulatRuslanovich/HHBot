package com.bipbup.handlers;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.impl.BasicStateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.bipbup.utils.CommandMessageConstants.BotCommand.CANCEL;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.MYQUERIES;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.NEWQUERY;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.COMMAND_CANCELLED;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.INVALID_INPUT;


@Slf4j
@RequiredArgsConstructor
public abstract class CancellableStateHandler implements StateHandler {

    protected final UserService userService;

    protected final ConfigService configService;

    private final BasicStateHandler basicStateHandler;

    protected boolean isCancelCommand(final String input) {
        return CANCEL.getCommand().equals(input);
    }

    protected boolean isBasicCommand(final String input) {
        return MYQUERIES.getCommand().equals(input)
                || NEWQUERY.getCommand().equals(input);
    }

    protected String processCancelCommand(final AppUser user) {
        userService.clearUserState(user.getTelegramId());
        log.info("User {} cancelled the command and state set to BASIC_STATE", user.getFirstName());
        return COMMAND_CANCELLED.getTemplate();
    }

    protected String processBasicCommand(final AppUser user, final String input) {
        return basicStateHandler.process(user, input);
    }

    protected String processInvalidInput(final AppUser user) {
        userService.clearUserState(user.getTelegramId());
        log.info("User {} provided an invalid input and state set to BASIC_STATE", user.getFirstName());
        return INVALID_INPUT.getTemplate();
    }

    protected String processConfigNotFoundMessage(final AppUser user) {
        var configId = configService.getSelectedConfigId(user.getTelegramId());
        configService.clearConfigSelection(user.getTelegramId());
        userService.clearUserState(user.getTelegramId());
        log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
        return CONFIG_NOT_FOUND.getTemplate();
    }

    protected AppUserConfig fetchConfig(final AppUser user) {
        var telegramId = user.getTelegramId();
        if (!isConfigUpdating(user))
            return fetchLastConfig(user);

        var configId = configService.getSelectedConfigId(telegramId);
        var optionalConfig = configService.getById(configId);

        if (optionalConfig.isPresent()) {
            configService.clearConfigSelection(telegramId);
            return optionalConfig.get();
        }

        return null;
    }

    protected AppUserConfig fetchLastConfig(final AppUser user) {
        List<AppUserConfig> configs = configService.getByUser(user);

        if (configs.isEmpty()) {
            log.warn("No configurations found for user {}", user.getFirstName());
            return null;
        }

        return configs.get(configs.size() - 1);
    }

    protected boolean isConfigUpdating(final AppUser user) {
        Long configId = configService.getSelectedConfigId(user.getTelegramId());
        return configId != null;
    }
}
