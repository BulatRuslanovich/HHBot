package com.bipbup.utils;


import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.impl.message.BasicStateHandler;
import com.bipbup.service.db.ConfigService;
import com.bipbup.service.cache.ConfigCacheService;
import com.bipbup.service.cache.UserStateCacheService;
import static com.bipbup.utils.CommandMessageConstants.AdminCommand.BROADCAST;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.CANCEL;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.HELP;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.MYQUERIES;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.NEWQUERY;
import static com.bipbup.utils.CommandMessageConstants.BotCommand.START;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.COMMAND_CANCELLED;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.INVALID_INPUT;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HandlerUtils {

    private final UserStateCacheService userStateCacheService;

    private final ConfigService configService;

    private final  ConfigCacheService configCacheService;

    private final BasicStateHandler basicStateHandler;

    public boolean isCancelCommand(String input) {
        return CANCEL.toString().equals(input);
    }

    public boolean isBasicCommand(String input) {
        return MYQUERIES.toString().equals(input)
               || NEWQUERY.toString().equals(input)
               || START.toString().equals(input)
               || HELP.toString().equals(input)
               || input.startsWith(BROADCAST.toString());
    }

    public boolean isConfigUpdating(AppUser user) {
        var configId = configCacheService.getConfigId(user.getTelegramId());
        return configId != null;
    }

    public String processCancelCommand(AppUser user) {
        userStateCacheService.clearUserState(user.getTelegramId());
        log.info("User {} cancelled the command and state set to BASIC_STATE", user.getFirstName());
        return COMMAND_CANCELLED.toString();
    }

    public String processBasicCommand(AppUser user, String input) {
        return basicStateHandler.process(user, input);
    }

    public String processInvalidInput(AppUser user) {
        userStateCacheService.clearUserState(user.getTelegramId());
        log.info("User {} provided an invalid input and state set to BASIC_STATE", user.getFirstName());
        return INVALID_INPUT.toString();
    }

    public String processConfigNotFoundMessage(AppUser user) {
        var configId = configCacheService.getConfigId(user.getTelegramId());
        configCacheService.clearConfigId(user.getTelegramId());
        userStateCacheService.clearUserState(user.getTelegramId());
        log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
        return CONFIG_NOT_FOUND.toString();
    }

    public Optional<AppUserConfig> fetchConfig(AppUser user) {
        if (!isConfigUpdating(user))
            return fetchConfigWithoutQuery(user);

        var telegramId = user.getTelegramId();
        var configId = configCacheService.getConfigId(telegramId);
        configCacheService.clearConfigId(telegramId);

        return configService.getConfigById(configId);
    }

    public Optional<AppUserConfig> fetchConfigWithoutQuery(AppUser user) {
        var configs = configService.getConfigByUser(user);

        return configs.stream()
                .filter(c -> Objects.isNull(c.getQueryText()))
                .findFirst()
                .or(() -> {
                    log.warn("No configurations found for user {}", user.getFirstName());
                    return Optional.empty();
                });
    }
}
