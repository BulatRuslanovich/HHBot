package com.bipbup.handlers.impl.message;

import com.bipbup.annotation.MessageQualifier;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.AppUserState;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.cache.UserStateCacheService;
import com.bipbup.service.db.ConfigService;
import com.bipbup.utils.HandlerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import static com.bipbup.enums.AppUserState.WAIT_EXCLUSION_STATE;
import static com.bipbup.utils.CommandMessageConstants.EMPTY;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.EMPTY_EXCLUSION_SET;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.EXCLUSION_SET;

@Slf4j
@Component
@MessageQualifier
@RequiredArgsConstructor
public class WaitExclusionStateHandler implements StateHandler {

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
        if (isInvalidExclusionText(input))
            return handlerUtils.processInvalidInput(user);

        var optionalConfig = handlerUtils.fetchConfig(user);

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            return processValidExclusion(user, config, input);
        } else {
            return handlerUtils.processInvalidInput(user);
        }
    }

    @Override
    public AppUserState state() {
        return WAIT_EXCLUSION_STATE;
    }

    private boolean isInvalidExclusionText(String input) {
        return !(input != null && !input.trim()
                .isEmpty() && input.length() <= MAX_QUERY_LENGTH);
    }

    private String processValidExclusion(AppUser user, AppUserConfig config, String input) {
        String output;

        if (EMPTY.equalsIgnoreCase(input)) {
            config.setExclusion(null);
            output = String.format(EMPTY_EXCLUSION_SET.toString(), config.getConfigName());
        } else {
            config.setExclusion(input);
            output = String.format(EXCLUSION_SET.toString(), input, config.getConfigName());
        }

        configService.saveConfig(config);
        userStateCacheService.clearUserState(user.getTelegramId());
        log.info("User {} set exclusion '{}' in configuration '{}'", user.getFirstName(), input, config.getConfigName());
        return output;
    }
}
