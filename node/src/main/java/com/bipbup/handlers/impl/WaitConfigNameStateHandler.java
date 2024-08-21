package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.CancellableStateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_EXISTS;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NAME_UPDATED;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.ENTER_QUERY;

@Slf4j
@Component
public class WaitConfigNameStateHandler extends CancellableStateHandler {

    protected static final int MAX_CONFIG_NAME_LENGTH = 50;

    public WaitConfigNameStateHandler(final UserService userService,
                                      final ConfigService configService,
                                      final BasicStateHandler basicStateHandler) {
        super(userService, configService, basicStateHandler);
    }


    @Override
    public String process(final AppUser user, final String input) {
        if (isCancelCommand(input))
            return processCancelCommand(user);
        if (isBasicCommand(input))
            return processBasicCommand(user, input);
        if (isInvalidConfigName(input))
            return processInvalidInput(user);
        if (isConfigExist(user, input))
            return processExistingConfig(user, input);
        if (isConfigUpdating(user))
            return processUpdatingConfig(user, input);

        return processNewConfig(user, input);
    }

    private boolean isInvalidConfigName(final String configName) {
        return !(configName != null
                && !configName.trim().isEmpty()
                && configName.length() <= MAX_CONFIG_NAME_LENGTH);
    }

    private boolean isConfigExist(final AppUser user, final String configName) {
        var configs = configService.getByUser(user);
        return configs.stream().anyMatch(config -> config.getConfigName().equals(configName));
    }

    private AppUserConfig createConfigWithOnlyName(final AppUser user, final String configName) {
        return AppUserConfig.builder()
                .configName(configName)
                .appUser(user)
                .build();
    }

    private String updateConfigName(final AppUser user,
                                    final AppUserConfig config,
                                    final String newConfigName) {
        var oldConfigName = config.getConfigName();
        config.setConfigName(newConfigName);
        configService.save(config);
        userService.clearUserState(user.getTelegramId());
        log.info("User {} updated name of config \"{}\" and state set to BASIC_STATE", user.getFirstName(), oldConfigName);
        return String.format(CONFIG_NAME_UPDATED.getTemplate(), oldConfigName, newConfigName);
    }

    private String processExistingConfig(final AppUser user, final String configName) {
        userService.clearUserState(user.getTelegramId());
        log.info("User {} attempted to create an existing config \"{}\" and state set to BASIC_STATE", user.getFirstName(), configName);
        return String.format(CONFIG_EXISTS.getTemplate(), configName);
    }

    private String processUpdatingConfig(final AppUser user, final String configName) {
        var telegramId = user.getTelegramId();
        var configId = configService.getSelectedConfigId(telegramId);
        configService.clearConfigSelection(telegramId);
        return configService.getById(configId)
                .map(c -> updateConfigName(user, c, configName))
                .orElse(processConfigNotFoundMessage(user));
    }

    private String processNewConfig(final AppUser user, final String configName) {
        var telegramId = user.getTelegramId();
        var newConfig = createConfigWithOnlyName(user, configName);
        configService.save(newConfig);
        configService.clearConfigSelection(telegramId);
        userService.saveUserState(telegramId, WAIT_QUERY_STATE);
        log.info("User {} created config \"{}\" and state set to WAIT_QUERY_STATE", user.getFirstName(), configName);
        return String.format(ENTER_QUERY.getTemplate(), configName);
    }
}
