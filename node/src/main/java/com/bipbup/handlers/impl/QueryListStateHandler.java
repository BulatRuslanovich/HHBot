package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.QUERY_MENU_STATE;
import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_FOUND_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.QUERY_OUTPUT_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.QUERY_PREFIX;

@Slf4j
@Component
public class QueryListStateHandler implements StateHandler {
    private final UserService userService;

    private final ConfigService configService;

    private final Decoder decoder;

    public QueryListStateHandler(UserService userService,
                                 ConfigService configService,
                                 Decoder decoder) {
        this.userService = userService;
        this.configService = configService;
        this.decoder = decoder;
    }

    @Override
    public String process(AppUser user, String input) {
        if (hasQueryPrefix(input)) return processQueryCommand(user, input);

        return "";
    }

    private boolean hasQueryPrefix(final String input) {
        return input.startsWith(QUERY_PREFIX);
    }
    
    private Pair<String, Boolean> generateQueryOutput(final long configId) {
        var optionalAppUserConfig = configService.getById(configId);

        if (optionalAppUserConfig.isEmpty()) return Pair.of(CONFIG_NOT_FOUND_MESSAGE, false);

        var config = optionalAppUserConfig.get();
        var answer = String.format(QUERY_OUTPUT_MESSAGE_TEMPLATE, config.getConfigName(), config.getQueryText());
        return Pair.of(answer, true);
    }

    private String processQueryCommand(AppUser user, String input) {
        var hash = input.substring(QUERY_PREFIX.length());
        var configId = decoder.idOf(hash);
        var answer = generateQueryOutput(configId);

        if (Boolean.TRUE.equals(answer.getSecond())) {
            userService.saveUserState(user.getTelegramId(), QUERY_MENU_STATE);
        }

        log.debug("User {} queried configuration with id {} and state set to QUERY_MENU_STATE", user.getFirstName(), configId);
        return answer.getFirst();
    }
}
