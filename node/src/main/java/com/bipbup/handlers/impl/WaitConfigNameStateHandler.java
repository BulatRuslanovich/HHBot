package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.Cancellable;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;
import static com.bipbup.utils.CommandMessageConstants.CONFIG_EXISTS_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_FOUND_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.CONFIG_NAME_UPDATED_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.ENTER_QUERY_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.INVALID_CONFIG_NAME_MESSAGE;

@Slf4j
@Component
public class WaitConfigNameStateHandler extends Cancellable implements StateHandler {

    private final ConfigService configService;

    protected static final int MAX_CONFIG_NAME_LENGTH = 50;

    public WaitConfigNameStateHandler(UserService userService,
                                      BasicStateHandler basicStateHandler,
                                      ConfigService configService) {
        super(userService, basicStateHandler);
        this.configService = configService;
    }


    @Override
    public String process(final AppUser user, final String input) {
        if (isCancelCommand(input)) return processCancelCommand(user);
        if (isBasicCommand(input)) return processBasicCommand(user, input);
        if (isInvalidConfigName(input)) return processInvalidConfigName(user);
        if (isConfigExist(user, input)) return processExistingConfig(user, input);
        if (isConfigUpdating(user)) return processUpdatingConfig(user, input);

        return processNewConfig(user, input);
    }

    private boolean isInvalidConfigName(final String configName) {
        return !(configName != null && !configName.trim().isEmpty() && configName.length() <= MAX_CONFIG_NAME_LENGTH);
    }

    private boolean isConfigExist(final AppUser user, final String configName) {
        var configs = configService.getByUser(user);
        return configs.stream().anyMatch(config -> config.getConfigName().equals(configName));
    }

    private boolean isConfigUpdating(final AppUser user) {
        var configId = configService.getSelectedConfigId(user.getTelegramId());
        return configId != null;
    }

    private AppUserConfig createConfigWithOnlyName(final AppUser user, final String configName) {
        return AppUserConfig.builder()
                .configName(configName)
                .appUser(user)
                .build();
    }

    private String updateConfigName(final AppUser user,
                                    final AppUserConfig config,
                                    final String configName) {
        config.setConfigName(configName);
        configService.save(config);
        userService.clearUserState(user.getTelegramId());
        log.debug("User {} updated config \"{}\" and state set to BASIC_STATE", user.getFirstName(), configName);
        return CONFIG_NAME_UPDATED_MESSAGE;
    }

    private String processExistingConfig(final AppUser user, final String configName) {
        userService.clearUserState(user.getTelegramId());
        log.debug("User {} attempted to create an existing config '{}'", user.getFirstName(), configName);
        return String.format(CONFIG_EXISTS_MESSAGE_TEMPLATE, configName);
    }

    private String processUpdatingConfig(final AppUser user, final String configName) {
        var configId = configService.getSelectedConfigId(user.getTelegramId());
        configService.clearConfigSelection(user.getTelegramId());
        return configService.getById(configId)
                .map(c -> updateConfigName(user, c, configName))
                .orElse(processConfigNotFoundMessage(user, configId));
    }

    private String processConfigNotFoundMessage(final AppUser user, final Long configId) {
        configService.clearConfigSelection(user.getTelegramId());
        userService.clearUserState(user.getTelegramId());
        log.warn("Config with id {} not found for user {}", configId, user.getFirstName());
        return CONFIG_NOT_FOUND_MESSAGE;
    }

    private String processNewConfig(final AppUser user, final String configName) {
        AppUserConfig newConfig = createConfigWithOnlyName(user, configName);
        configService.save(newConfig);
        userService.saveUserState(user.getTelegramId(), WAIT_QUERY_STATE);
        log.debug("User {} created config \"{}\" and state set to WAIT_QUERY_STATE", user.getFirstName(), configName);
        return String.format(ENTER_QUERY_MESSAGE_TEMPLATE, configName);
    }

    private String processInvalidConfigName(AppUser user) {
        userService.clearUserState(user.getTelegramId());
        log.debug("User {} provided an invalid config name.", user.getFirstName());
        return INVALID_CONFIG_NAME_MESSAGE;
    }
}
