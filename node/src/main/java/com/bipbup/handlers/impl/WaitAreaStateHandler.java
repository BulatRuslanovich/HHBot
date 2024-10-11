package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.CancellableStateHandler;
import com.bipbup.service.AreaService;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.bipbup.utils.CommandMessageConstants.ANY;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.ANY_AREA_SET;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.AREA_SET;

@Slf4j
@Component
public class WaitAreaStateHandler extends CancellableStateHandler {

    private final AreaService areaService;

    @Autowired
    public WaitAreaStateHandler(UserService userService,
                                ConfigService configService,
                                BasicStateHandler basicStateHandler,
                                AreaService areaService) {
        super(userService, configService, basicStateHandler);
        this.areaService = areaService;
    }

    private static String getOutputSetArea(AppUserConfig config, String input, String area) {
        if (!input.equalsIgnoreCase(ANY)) {
            config.setArea(area);
            return String.format(AREA_SET.getTemplate(), area, config.getConfigName());
        } else {
            config.setArea(null);
            return String.format(ANY_AREA_SET.getTemplate(), config.getConfigName());
        }
    }

    private static String normalizeAreaName(String input) {
        if (input.equalsIgnoreCase(ANY)) return input;
        var separator = input.contains("-") ? "-" : " ";
        return Arrays.stream(input.split(separator))
                .map(String::toLowerCase)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(separator));
    }

    @Override
    public String process(AppUser user, String input) {
        if (isCancelCommand(input))
            return processCancelCommand(user);
        if (isBasicCommand(input))
            return processBasicCommand(user, input);
        if (isInvalidAreaName(input))
            return processInvalidInput(user);

        var optionalConfig = fetchConfig(user);
        return optionalConfig.map(config -> processValidAreaName(user, config, input))
                .orElseGet(() -> processConfigNotFoundMessage(user));
    }

    private String processValidAreaName(AppUser user,
                                        AppUserConfig config,
                                        String input) {
        var area = normalizeAreaName(input);
        var output = getOutputSetArea(config, input, area);

        configService.save(config);
        userService.clearUserState(user.getTelegramId());

        log.info("User {} set area '{}' in configuration '{}'", user.getFirstName(), area, config.getConfigName());
        return output;
    }

    private boolean isInvalidAreaName(String input) {
        return !(input != null
                && !input.trim().isEmpty()
                && (input.equalsIgnoreCase(ANY)
                || areaService.getAreaIdByName(input) != null));
    }
}
