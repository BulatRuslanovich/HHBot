package com.bipbup.handlers.impl.callback;

import com.bipbup.annotation.CallbackQualifier;
import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import static com.bipbup.enums.AppUserState.QUERY_DELETE_STATE;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.db.ConfigService;
import com.bipbup.service.cache.UserStateCacheService;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.QUERY_LIST_STATE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_DELETED;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.USER_QUERIES;
import static com.bipbup.utils.CommandMessageConstants.Prefix;

@Slf4j
@Component
@CallbackQualifier
@RequiredArgsConstructor
public class QueryDeleteStateHandler implements StateHandler {

    private final UserStateCacheService userStateCacheService;

    private final ConfigService configService;

    private final QueryListStateHandler queryListStateHandler;

    private final Decoder decoder;

    @Override
    public String process(AppUser user, String input) {
        if (hasDeleteConfirmPrefix(input))
            return processDeleteConfirmCommand(user, input);
        if (isDeleteCancelCommand(input))
            return processDeleteCancelCommand(user, input);

        return "";
    }

    @Override
    public AppUserState state() {
        return QUERY_DELETE_STATE;
    }

    private boolean isDeleteCancelCommand(String input) {
        return input.startsWith(Prefix.QUERY);
    }

    private boolean hasDeleteConfirmPrefix(String input) {
        return input.startsWith(Prefix.DELETE_CONFIRM);
    }

    private String processDeleteConfirmCommand(AppUser user, String input) {
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getConfigById(configId);

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            configService.deleteConfig(config);

            var count = configService.countOfConfigs(user);
            String addingString = "";

            if (count == 0) {
                userStateCacheService.clearUserState(user.getTelegramId());
            } else {
                userStateCacheService.putUserState(user.getTelegramId(), QUERY_LIST_STATE);
                addingString = "\n" + USER_QUERIES;
            }

            log.info("User {} deleted configuration with id {} and state set to QUERY_LIST_STATE",
                    user.getFirstName(), configId);
            return String.format(CONFIG_DELETED.toString(), config.getConfigName()) + addingString;
        } else {
            userStateCacheService.clearUserState(user.getTelegramId());
            log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND.toString();
        }
    }

    private String processDeleteCancelCommand(AppUser user, String input) {
        userStateCacheService.putUserState(user.getTelegramId(), QUERY_LIST_STATE);
        log.info("User {} chose not to delete the configuration and state set to QUERY_LIST_STATE",
                user.getFirstName());
        return queryListStateHandler.process(user, input);
    }
}
