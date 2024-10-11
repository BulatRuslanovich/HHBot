package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.CancellableStateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_EXISTS;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NAME_UPDATED;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.ENTER_QUERY;

@Slf4j
@Component
public class WaitConfigNameStateHandler extends CancellableStateHandler {

    protected static final int MAX_CONFIG_NAME_LENGTH = 50;

    @Autowired
    public WaitConfigNameStateHandler(UserService userService,
                                      ConfigService configService,
                                      BasicStateHandler basicStateHandler) {
        super(userService, configService, basicStateHandler);
    }

    @Override
    public String process(AppUser user, String input) {
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

    private boolean isInvalidConfigName(String configName) {
        return !(configName != null
                && !configName.trim().isEmpty()
                && configName.length() <= MAX_CONFIG_NAME_LENGTH);
    }

    private boolean isConfigExist(AppUser user, String configName) {
        var configs = configService.getByUser(user);
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
        configService.save(config);
        userService.clearUserState(user.getTelegramId());
        log.info("User {} updated name of config \"{}\" and state set to BASIC_STATE", user.getFirstName(), oldConfigName);
        return String.format(CONFIG_NAME_UPDATED.getTemplate(), oldConfigName, newConfigName);
    }

    private String processExistingConfig(AppUser user, String configName) {
        userService.clearUserState(user.getTelegramId());
        log.info("User {} attempted to create an existing config \"{}\" and state set to BASIC_STATE", user.getFirstName(), configName);
        return String.format(CONFIG_EXISTS.getTemplate(), configName);
    }

    private String processUpdatingConfig(AppUser user, String configName) {
        var telegramId = user.getTelegramId();
        var configId = configService.getSelectedConfigId(telegramId);
        configService.clearConfigSelection(telegramId);

        var optionalConfig = configService.getById(configId);

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            return updateConfigName(user, config, configName);
        } else {
            return processConfigNotFoundMessage(user);
        }
    }

    private String processNewConfig(AppUser user, String configName) {
        var telegramId = user.getTelegramId();
        var newConfig = createConfigWithOnlyName(user, configName);
        configService.save(newConfig);
        configService.clearConfigSelection(telegramId);
        userService.saveUserState(telegramId, WAIT_QUERY_STATE);
        log.info("User {} created config \"{}\" and state set to WAIT_QUERY_STATE", user.getFirstName(), configName);
        return String.format(ENTER_QUERY.getTemplate(), configName);
    }
}
