package com.bipbup.handlers;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.impl.BasicStateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Optional;

import static com.bipbup.utils.CommandMessageConstants.AdminCommand.BROADCAST;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.*;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.COMMAND_CANCELLED;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.INVALID_INPUT;


@Slf4j
@RequiredArgsConstructor
public abstract class CancellableStateHandler implements StateHandler {

    protected final UserService userService;

    protected final ConfigService configService;

    private final BasicStateHandler basicStateHandler;

    protected boolean isCancelCommand(String input) {
        return CANCEL.getCommand().equals(input);
    }

    protected boolean isBasicCommand(String input) {
        return MYQUERIES.getCommand().equals(input)
                || NEWQUERY.getCommand().equals(input)
                || START.getCommand().equals(input)
                || HELP.getCommand().equals(input)
                || input.startsWith(BROADCAST.getCommand());
    }

    protected boolean isConfigUpdating(AppUser user) {
        var configId = configService.getSelectedConfigId(user.getTelegramId());
        return configId != null;
    }

    protected String processCancelCommand(AppUser user) {
        userService.clearUserState(user.getTelegramId());
        log.info("User {} cancelled the command and state set to BASIC_STATE", user.getFirstName());
        return COMMAND_CANCELLED.getTemplate();
    }

    protected String processBasicCommand(AppUser user, String input) {
        return basicStateHandler.process(user, input);
    }

    protected String processInvalidInput(AppUser user) {
        userService.clearUserState(user.getTelegramId());
        log.info("User {} provided an invalid input and state set to BASIC_STATE", user.getFirstName());
        return INVALID_INPUT.getTemplate();
    }

    protected String processConfigNotFoundMessage(AppUser user) {
        var configId = configService.getSelectedConfigId(user.getTelegramId());
        configService.clearConfigSelection(user.getTelegramId());
        userService.clearUserState(user.getTelegramId());
        log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
        return CONFIG_NOT_FOUND.getTemplate();
    }

    protected Optional<AppUserConfig> fetchConfig(AppUser user) {
        if (!isConfigUpdating(user))
            return fetchLastConfig(user);

        var telegramId = user.getTelegramId();
        var configId = configService.getSelectedConfigId(telegramId);
        configService.clearConfigSelection(telegramId);
        return configService.getById(configId);
    }

    protected Optional<AppUserConfig> fetchLastConfig(AppUser user) {
        var configs = configService.getByUser(user);

        return configs.stream()
                .filter(c -> Objects.isNull(c.getQueryText()))
                .findFirst()
                .or(() -> {
                    log.warn("No configurations found for user {}", user.getFirstName());
                    return Optional.empty();
                });
    }
}
