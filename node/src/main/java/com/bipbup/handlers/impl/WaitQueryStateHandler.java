package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.Cancellable;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class WaitQueryStateHandler extends Cancellable implements StateHandler {

    private final ConfigService configService;

    protected static final int MAX_QUERY_LENGTH = 50;
    protected static final String INVALID_QUERY_MESSAGE = "Некорректный запрос. Пожалуйста, проверьте введенные данные.";
    protected static final String QUERY_SET_MESSAGE_TEMPLATE = "Запрос \"%s\" успешно установлен в конфигурации \"%s\".";
    protected static final String CONFIG_NOT_FOUND_MESSAGE = "Произошла ошибка. Попробуйте ещё раз.";

    public WaitQueryStateHandler(BasicStateHandler basicStateHandler,
                                 UserService userService,
                                 ConfigService configService) {
        super(userService, basicStateHandler);
        this.configService = configService;
    }

    @Override
    public String process(final AppUser user, final String input) {
        if (isCancelCommand(input)) return processCancelCommand(user);
        if (isBasicCommand(input)) return processBasicCommand(user, input);
        if (isInvalidQueryText(input)) return processInvalidQuery(user);

        AppUserConfig config = fetchConfig(user);

        if (config == null) return processConfigNotFoundMessage(user);

        return processValidQuery(user, config, input);
    }

    private boolean isConfigUpdating(final AppUser user) {
        Long configId = configService.getSelectedConfigId(user.getTelegramId());
        return configId != null;
    }

    private boolean isInvalidQueryText(final String input) {
        return !(input != null
                && !input.trim().isEmpty()
                && input.length() <= MAX_QUERY_LENGTH);
    }

    private AppUserConfig fetchConfig(final AppUser user) {
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

    private AppUserConfig fetchLastConfig(final AppUser user) {
        List<AppUserConfig> configs = configService.getByUser(user);

        if (configs.isEmpty()) {
            log.error("No configurations found for user.");
            throw new IllegalStateException("No configurations found for user.");
        }

        return configs.get(configs.size() - 1);
    }

    private String processConfigNotFoundMessage(final AppUser user) {
        var configId = configService.getSelectedConfigId(user.getTelegramId());
        configService.clearConfigSelection(user.getTelegramId());
        userService.clearUserState(user.getTelegramId());
        log.warn("Config with id {} not found for user {}", configId, user.getTelegramId());
        return CONFIG_NOT_FOUND_MESSAGE;
    }

    private String processInvalidQuery(final AppUser user) {
        userService.clearUserState(user.getTelegramId());
        log.debug("User {} provided an invalid query and state set to BASIC_STATE.", user.getFirstName());
        return INVALID_QUERY_MESSAGE;
    }

    private String processValidQuery(final AppUser user, final AppUserConfig config, final String input) {
        config.setQueryText(input);
        configService.save(config);
        userService.clearUserState(user.getTelegramId());
        log.info("User {} set query '{}' in configuration '{}'", user.getFirstName(), input, config.getConfigName());
        return String.format(QUERY_SET_MESSAGE_TEMPLATE, input, config.getConfigName());
    }
}
