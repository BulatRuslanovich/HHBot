package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.QUERY_MENU_STATE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.QUERY_OUTPUT;
import static com.bipbup.utils.CommandMessageConstants.Prefix;
import static java.lang.Boolean.TRUE;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryListStateHandler implements StateHandler {

    private final UserService userService;

    private final ConfigService configService;

    private final Decoder decoder;

    @Override
    public String process(AppUser user, String input) {
        if (hasQueryPrefix(input)) return processQueryCommand(user, input);

        return "";
    }

    private boolean hasQueryPrefix(String input) {
        return input.startsWith(Prefix.QUERY);
    }
    
    private Pair<Boolean, String> generateQueryOutput(long configId) {
        var optionalConfig = configService.getById(configId);

        if (optionalConfig.isEmpty())
            return Pair.of(false, CONFIG_NOT_FOUND.getTemplate());

        var config = optionalConfig.get();
        var answer = String.format(QUERY_OUTPUT.getTemplate(), config.getConfigName(), config.getQueryText());
        return Pair.of(true, answer);
    }

    private String processQueryCommand(AppUser user, String input) {
        var configId = decoder.parseIdFromCallback(input);
        var answer = generateQueryOutput(configId);

        if (TRUE.equals(answer.getFirst())) {
            userService.saveUserState(user.getTelegramId(), QUERY_MENU_STATE);
            log.info("User {} queried configuration with id {} and state set to QUERY_MENU_STATE", user.getFirstName(), configId);
        }

        return answer.getSecond();
    }
}
