package com.bipbup.handlers.impl.callback;

import com.bipbup.annotation.CallbackQualifier;
import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import static com.bipbup.enums.AppUserState.QUERY_UPDATE_STATE;
import static com.bipbup.enums.AppUserState.WAIT_AREA_STATE;
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import static com.bipbup.enums.AppUserState.WAIT_EDUCATION_STATE;
import static com.bipbup.enums.AppUserState.WAIT_EXPERIENCE_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;
import static com.bipbup.enums.AppUserState.WAIT_SCHEDULE_STATE;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.db.ConfigService;
import com.bipbup.service.cache.ConfigCacheService;
import com.bipbup.service.cache.UserStateCacheService;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.ENTER_AREA;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.ENTER_CONFIG_NAME;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.ENTER_QUERY;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.SELECT_EDUCATION;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.SELECT_EXPERIENCE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.SELECT_SCHEDULE;
import static com.bipbup.utils.CommandMessageConstants.Prefix;
import com.bipbup.utils.Decoder;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@CallbackQualifier
@RequiredArgsConstructor
public class QueryUpdateStateHandler implements StateHandler {

    private final UserStateCacheService userStateCacheService;

    private final ConfigService configService;

    private final Decoder decoder;

    private final QueryListStateHandler queryListStateHandler;

    private final ConfigCacheService configCacheService;

    private static final Map<String, ActionInfo> ACTION_PROPERTIES_MAP = Map.of(
            Prefix.UPDATE_CONFIG_NAME,
            new ActionInfo(WAIT_CONFIG_NAME_STATE, ENTER_CONFIG_NAME.toString(), true),
            Prefix.UPDATE_QUERY,
            new ActionInfo(WAIT_QUERY_STATE, ENTER_QUERY.toString(), true),
            Prefix.UPDATE_EXPERIENCE,
            new ActionInfo(WAIT_EXPERIENCE_STATE, SELECT_EXPERIENCE.toString(), false),
            Prefix.UPDATE_AREA,
            new ActionInfo(WAIT_AREA_STATE, ENTER_AREA.toString(), true),
            Prefix.UPDATE_EDUCATION,
            new ActionInfo(WAIT_EDUCATION_STATE, SELECT_EDUCATION.toString(), false),
            Prefix.UPDATE_SCHEDULE,
            new ActionInfo(WAIT_SCHEDULE_STATE, SELECT_SCHEDULE.toString(), false)
    );

    @Override
    public String process(AppUser user, String input) {
        if (isBackToQueryMenuCommand(input))
            return processBackToQueryMenuCommand(user, input);
        if (hasUpdatePrefix(input))
            return processUpdateConfigCommand(user, input);

        return "";
    }

    @Override
    public AppUserState state() {
        return QUERY_UPDATE_STATE;
    }

    private boolean hasUpdatePrefix(String input) {
        return input.startsWith(Prefix.UPDATE_STATE);
    }

    private String processBackToQueryMenuCommand(AppUser user, String input) {
        return queryListStateHandler.process(user, input);
    }

    private boolean isBackToQueryMenuCommand(String input) {
        return input.startsWith(Prefix.QUERY);
    }

    private String processUpdateConfigCommand(AppUser user, String input) {
        var prefix = input.substring(0, input.lastIndexOf('_') + 1);
        var properties = ACTION_PROPERTIES_MAP.get(prefix);

        return updateConfigSelectionAndUserState(user, input, properties);
    }

    private String updateConfigSelectionAndUserState(AppUser user,
                                                     String input,
                                                     ActionInfo properties) {
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getConfigById(configId);

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            userStateCacheService.putUserState(user.getTelegramId(), properties.state());

            if (properties.saveSelection())
                configCacheService.putConfigId(user.getTelegramId(), configId);


            log.info("User {} selected parameter to edit and state set to {}", user.getFirstName(), properties.state());
            return String.format(properties.output(), config.getConfigName());
        } else {
            userStateCacheService.clearUserState(user.getTelegramId());
            log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND.toString();
        }
    }
}
