package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.CancellableStateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.QUERY_SET;

@Slf4j
@Component
public class WaitQueryStateHandler extends CancellableStateHandler {

    protected static final int MAX_QUERY_LENGTH = 50;

    @Autowired
    public WaitQueryStateHandler(UserService userService,
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
        if (isInvalidQueryText(input))
            return processInvalidInput(user);

        var optionalConfig = fetchConfig(user);
        return optionalConfig.map(config -> processValidQuery(user, config, input))
                .orElseGet(() -> processConfigNotFoundMessage(user));
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
        configService.save(config);
        userService.clearUserState(user.getTelegramId());
        log.info("User {} set query '{}' in configuration '{}'", user.getFirstName(), input, config.getConfigName());
        return String.format(QUERY_SET.getTemplate(), input, config.getConfigName());
    }
}
