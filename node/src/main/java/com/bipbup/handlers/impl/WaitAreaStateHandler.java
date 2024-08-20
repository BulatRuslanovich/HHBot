package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.CancellableStateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.AreaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.bipbup.utils.CommandMessageConstants.AREA_SET_MESSAGE_TEMPLATE;

@Slf4j
@Component
public class WaitAreaStateHandler extends CancellableStateHandler {

    protected static final int MAX_AREA_NAME_LENGTH = 30;

    public WaitAreaStateHandler(final UserService userService,
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
        if (isInvalidAreaName(input))
            return processInvalidInput(user);

        var config = fetchConfig(user);
        if (config == null)
            return processConfigNotFoundMessage(user);

        return processValidAreaName(user, config, input);
    }

    private String processValidAreaName(final AppUser user,
                                        final AppUserConfig config,
                                        final String input) {
        var separator = input.contains("-") ? "-" : " ";
        var area = Arrays.stream(input.split(separator))
                .map(String::toLowerCase)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(separator));

        config.setArea(area);
        configService.save(config);
        userService.clearUserState(user.getTelegramId());

        log.info("User {} set area '{}' in configuration '{}'", user.getFirstName(), area, config.getConfigName());
        return String.format(AREA_SET_MESSAGE_TEMPLATE, area, config.getConfigName());
    }

    private boolean isInvalidAreaName(final String input) {
        return !(input != null
                && !input.trim().isEmpty()
                && input.length() <= MAX_AREA_NAME_LENGTH
                && AreaUtil.getAreaIdByName(input) != null);
    }
}
