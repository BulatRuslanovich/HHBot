package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.cache.UserStateCacheService;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.QUERY_SET;
import com.bipbup.utils.HandlerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitQueryStateHandler implements StateHandler {

    protected static final int MAX_QUERY_LENGTH = 50;

    private final ConfigService configService;

    private final UserStateCacheService userStateCacheService;

    private final HandlerUtils handlerUtils;

    @Override
    public String process(AppUser user, String input) {
        if (handlerUtils.isCancelCommand(input))
            return handlerUtils.processCancelCommand(user);
        if (handlerUtils.isBasicCommand(input))
            return handlerUtils.processBasicCommand(user, input);
        if (isInvalidQueryText(input))
            return handlerUtils.processInvalidInput(user);

        var optionalConfig = handlerUtils.fetchConfig(user);
        return optionalConfig.map(config -> processValidQuery(user, config, input))
                .orElseGet(() -> handlerUtils.processConfigNotFoundMessage(user));
    }

    private boolean isInvalidQueryText(String input) {
        return !(input != null
                && !input.trim().isEmpty()
                && input.length() <= MAX_QUERY_LENGTH);
    }

    private String processValidQuery(AppUser user,
                                     AppUserConfig config,
                                     String input) {
        config.setQueryText(input);
        configService.saveConfig(config, false);
        userStateCacheService.clearUserState(user.getTelegramId());
        log.info("User {} set query '{}' in configuration '{}'", user.getFirstName(), input, config.getConfigName());
        return String.format(QUERY_SET.getTemplate(), input, config.getConfigName());
    }
}
