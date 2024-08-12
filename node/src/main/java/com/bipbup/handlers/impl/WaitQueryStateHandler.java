package com.bipbup.handlers.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.Cancellable;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.ConfigUtil;
import com.bipbup.utils.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class WaitQueryStateHandler extends Cancellable implements StateHandler {

    private final AppUserConfigDAO appUserConfigDAO;

    private final ConfigUtil configUtil;

    protected static final int MAX_QUERY_LENGTH = 50;
    protected static final String INVALID_QUERY_MESSAGE = "Некорректный запрос. Пожалуйста, проверьте введенные данные.";
    protected static final String QUERY_SET_MESSAGE_TEMPLATE = "Запрос \"%s\" успешно установлен в конфигурации \"%s\".";
    protected static final String CONFIG_NOT_FOUND_MESSAGE = "Произошла ошибка. Попробуйте ещё раз.";

    public WaitQueryStateHandler(AppUserDAO appUserDAO,
                                 BasicStateHandler basicStateHandler,
                                 UserUtil userUtil,
                                 AppUserConfigDAO appUserConfigDAO,
                                 ConfigUtil configUtil) {
        super(appUserDAO, userUtil, basicStateHandler);
        this.appUserConfigDAO = appUserConfigDAO;
        this.configUtil = configUtil;
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
        Long configId = configUtil.getSelectedConfigId(user.getTelegramId());
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

        var configId = configUtil.getSelectedConfigId(user.getTelegramId());
        var optionalConfig = appUserConfigDAO.findById(configId);

        if (optionalConfig.isPresent()) {
            configUtil.clearConfigSelection(user.getTelegramId());
            return optionalConfig.get();
        }

        return null;
    }

    private AppUserConfig fetchLastConfig(final AppUser user) {
        List<AppUserConfig> configs = appUserConfigDAO.findByAppUser(user);

        if (configs.isEmpty()) {
            log.error("No configurations found for user.");
            throw new IllegalStateException("No configurations found for user.");
        }

        return configs.get(configs.size() - 1);
    }

    private String processConfigNotFoundMessage(final AppUser user) {
        var configId = configUtil.getSelectedConfigId(user.getTelegramId()); //TODO: не понимаю зачем нам айди мертвых душ
        configUtil.clearConfigSelection(user.getTelegramId());
        userUtil.clearUserState(user.getTelegramId());
        log.warn("Config with id {} not found for user {}", configId, user.getTelegramId());
        return CONFIG_NOT_FOUND_MESSAGE;
    }

    private String processInvalidQuery(final AppUser user) {
        userUtil.clearUserState(user.getTelegramId());
        log.debug("User {} provided an invalid query and state set to BASIC_STATE.", user.getFirstName());
        return INVALID_QUERY_MESSAGE;
    }

    private String processValidQuery(final AppUser user, final AppUserConfig config, final String input) {
        config.setQueryText(input);
        appUserConfigDAO.saveAndFlush(config);
        userUtil.clearUserState(user.getTelegramId());
        log.info("User {} set query '{}' in configuration '{}'", user.getFirstName(), input, config.getConfigName());
        return String.format(QUERY_SET_MESSAGE_TEMPLATE, input, config.getConfigName());
    }
}
