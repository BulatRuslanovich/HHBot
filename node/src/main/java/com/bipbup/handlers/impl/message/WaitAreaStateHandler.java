package com.bipbup.handlers.impl.message;

import com.bipbup.annotation.MessageQualifier;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.AppUserState;
import static com.bipbup.enums.AppUserState.WAIT_AREA_STATE;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.net.AreaService;
import com.bipbup.service.db.ConfigService;
import com.bipbup.service.cache.UserStateCacheService;
import com.bipbup.utils.HandlerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.bipbup.utils.CommandMessageConstants.ANY;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.ANY_AREA_SET;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.AREA_SET;

@Slf4j
@Component
@MessageQualifier
@RequiredArgsConstructor
public class WaitAreaStateHandler implements StateHandler {

    private final AreaService areaService;

    private final ConfigService configService;

    private final UserStateCacheService userStateCacheService;

    private final HandlerUtils handlerUtils;

    private String getOutputAndSetArea(AppUserConfig config, String input, String area) {
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
        if (handlerUtils.isCancelCommand(input))
            return handlerUtils.processCancelCommand(user);
        if (handlerUtils.isBasicCommand(input))
            return handlerUtils.processBasicCommand(user, input);
        if (isInvalidAreaName(input))
            return handlerUtils.processInvalidInput(user);

        var optionalConfig = handlerUtils.fetchConfig(user);
        return optionalConfig.map(config -> processValidAreaName(user, config, input))
                .orElseGet(() -> handlerUtils.processConfigNotFoundMessage(user));
    }

    @Override
    public AppUserState state() {
        return WAIT_AREA_STATE;
    }

    private String processValidAreaName(AppUser user,
                                        AppUserConfig config,
                                        String input) {
        var area = normalizeAreaName(input);
        var output = getOutputAndSetArea(config, input, area);

        configService.saveConfig(config, false);
        userStateCacheService.clearUserState(user.getTelegramId());

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
