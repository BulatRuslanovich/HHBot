package com.bipbup.handlers.impl.message;

import com.bipbup.annotation.MessageQualifier;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.AppUserState;
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.db.ConfigService;
import com.bipbup.service.cache.ConfigCacheService;
import com.bipbup.service.cache.UserStateCacheService;
import com.bipbup.utils.HandlerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_EXISTS;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NAME_UPDATED;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.ENTER_QUERY;

@Slf4j
@Component
@MessageQualifier
@RequiredArgsConstructor
public class WaitConfigNameStateHandler implements StateHandler {

    private static final int MAX_CONFIG_NAME_LENGTH = 50;

    private final ConfigService configService;

    private final UserStateCacheService userStateCacheService;

    private final ConfigCacheService configCacheService;

    private final HandlerUtils handlerUtils;

    @Override
    public String process(AppUser user, String input) {
        String output;
        if (handlerUtils.isCancelCommand(input))
            output = handlerUtils.processCancelCommand(user);
        else if (handlerUtils.isBasicCommand(input))
            output = handlerUtils.processBasicCommand(user, input);
        else if (isInvalidConfigName(input))
            output = handlerUtils.processInvalidInput(user);
        else if (isConfigExist(user, input))
            output = processExistingConfig(user, input);
        else if (handlerUtils.isConfigUpdating(user))
            output = processUpdatingConfig(user, input);
        else {
            output = processNewConfig(user, input);
        }

        return output;
    }

    @Override
    public AppUserState state() {
        return WAIT_CONFIG_NAME_STATE;
    }

    private boolean isInvalidConfigName(String configName) {
        return !(configName != null
                && !configName.trim().isEmpty()
                && configName.length() <= MAX_CONFIG_NAME_LENGTH);
    }

    private boolean isConfigExist(AppUser user, String configName) {
        var configs = configService.getConfigByUser(user);
        return configs.stream().anyMatch(config -> config.getConfigName().equals(configName));
    }

    private AppUserConfig createConfigWithOnlyName(AppUser user, String configName) {
        return AppUserConfig.builder()
                .configName(configName)
                .appUser(user)
                .build();
    }

    private String updateConfigName(AppUser user,
                                    AppUserConfig config,
                                    String newConfigName) {
        var oldConfigName = config.getConfigName();
        config.setConfigName(newConfigName);
        configService.saveConfig(config, false);
        userStateCacheService.clearUserState(user.getTelegramId());
        log.info("User {} updated name of config \"{}\" and state set to BASIC_STATE",
                user.getFirstName(), oldConfigName);
        return String.format(CONFIG_NAME_UPDATED.getTemplate(), oldConfigName, newConfigName);
    }

    private String processExistingConfig(AppUser user, String configName) {
        userStateCacheService.clearUserState(user.getTelegramId());
        log.info("User {} attempted to create an existing config \"{}\" and state set to BASIC_STATE",
                user.getFirstName(), configName);
        return String.format(CONFIG_EXISTS.getTemplate(), configName);
    }

    private String processUpdatingConfig(AppUser user, String configName) {
        var telegramId = user.getTelegramId();
        var configId = configCacheService.getConfigId(telegramId);
        configCacheService.clearConfigId(telegramId);

        var optionalConfig = configService.getConfigById(configId);

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            return updateConfigName(user, config, configName);
        } else {
            return handlerUtils.processConfigNotFoundMessage(user);
        }
    }

    private String processNewConfig(AppUser user, String configName) {
        var telegramId = user.getTelegramId();
        var newConfig = createConfigWithOnlyName(user, configName);
        configService.saveConfig(newConfig, false);
        configCacheService.clearConfigId(telegramId);
        userStateCacheService.putUserState(telegramId, WAIT_QUERY_STATE);
        log.info("User {} created config \"{}\" and state set to WAIT_QUERY_STATE", user.getFirstName(), configName);
        return String.format(ENTER_QUERY.getTemplate(), configName);
    }
}
