package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.CancellableStateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.utils.CommandMessageConstants.QUERY_SET_MESSAGE_TEMPLATE;

@Slf4j
@Component
public class WaitQueryStateHandler extends CancellableStateHandler {

    protected static final int MAX_QUERY_LENGTH = 50;

    public WaitQueryStateHandler(final UserService userService,
                                 final ConfigService configService,
                                 final BasicStateHandler basicStateHandler) {
        super(userService, configService, basicStateHandler);
    }

    @Override
    public String process(final AppUser user, final String input) {
        if (isCancelCommand(input)) return processCancelCommand(user);
        if (isBasicCommand(input)) return processBasicCommand(user, input);
        if (isInvalidQueryText(input)) return processInvalidInput(user);

        AppUserConfig config = fetchConfig(user);
        if (config == null) return processConfigNotFoundMessage(user);

        return processValidQuery(user, config, input);
    }

    private boolean isInvalidQueryText(final String input) {
        return !(input != null
                && !input.trim().isEmpty()
                && input.length() <= MAX_QUERY_LENGTH);
    }

    private String processValidQuery(final AppUser user, final AppUserConfig config, final String input) {
        config.setQueryText(input);
        configService.save(config);
        userService.clearUserState(user.getTelegramId());
        log.info("User {} set query '{}' in configuration '{}'", user.getFirstName(), input, config.getConfigName());
        return String.format(QUERY_SET_MESSAGE_TEMPLATE, input, config.getConfigName());
    }
}
