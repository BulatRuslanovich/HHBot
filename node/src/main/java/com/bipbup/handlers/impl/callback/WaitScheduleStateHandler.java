package com.bipbup.handlers.impl.callback;

import com.bipbup.annotation.CallbackQualifier;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.ScheduleType;
import com.bipbup.enums.AppUserState;
import static com.bipbup.enums.AppUserState.WAIT_SCHEDULE_STATE;
import com.bipbup.enums.impl.ScheduleTypeParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.db.ConfigService;
import com.bipbup.service.cache.ScheduleTypeCacheService;
import com.bipbup.service.cache.UserStateCacheService;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.SCHEDULE_SAVE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.SELECT_SCHEDULE;
import static com.bipbup.utils.CommandMessageConstants.Prefix;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@CallbackQualifier
@RequiredArgsConstructor
public class WaitScheduleStateHandler implements StateHandler {

    private final ConfigService configService;

    private final UserStateCacheService userStateCacheService;

    private final Decoder decoder;

    private final ScheduleTypeCacheService scheduleTypeCacheService;

    @Override
    public String process(AppUser user, String input) {
        if (hasSavePrefix(input)) {
            return processSaveScheduleTypesCommand(user, input);
        }
        if (hasSchedulePrefix(input)) {
            return processSetScheduleTypeCommand(user, input);
        }

        return "";
    }

    @Override
    public AppUserState state() {
        return WAIT_SCHEDULE_STATE;
    }

    private boolean hasSavePrefix(String input) {
        return input.startsWith(Prefix.SCHEDULE_SAVE);
    }

    private boolean hasSchedulePrefix(String input) {
        return input.startsWith(Prefix.WAIT_SCHEDULE_STATE);
    }

    private String processConfigNotFoundMessage(AppUser user, long configId) {
        userStateCacheService.clearUserState(user.getTelegramId());
        log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
        return CONFIG_NOT_FOUND.getTemplate();
    }


    protected String processSaveScheduleTypesCommand(AppUser user, String input) {
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getConfigById(configId);

        if (optionalConfig.isPresent()) {
            var telegramId = user.getTelegramId();
            var config = optionalConfig.get();
            var cashedScheduleTypes = scheduleTypeCacheService.getScheduleTypes(telegramId);

            var scheduleTypes = cashedScheduleTypes.stream()
                    .map(s -> new ScheduleType(null, s, config))
                    .toList();

            config.setScheduleTypes(scheduleTypes);
            
            configService.saveConfig(config);

            scheduleTypeCacheService.clearScheduleTypes(telegramId);
            userStateCacheService.clearUserState(telegramId);

            log.info("User {} saved schedule types for configuration {} and state set to BASIC_STATE",
                    user.getFirstName(), config.getConfigName());
            return String.format(SCHEDULE_SAVE.getTemplate(), config.getConfigName());
        } else {
            return processConfigNotFoundMessage(user, configId);
        }
    }

    private String processSetScheduleTypeCommand(AppUser user, String input) {
        var prefix = input.substring(0, input.lastIndexOf('_') + 1);
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getConfigById(configId);

        if (optionalConfig.isPresent()) {
            var telegramId = user.getTelegramId();
            var config = optionalConfig.get();
            var currentScheduleType = ScheduleTypeParam.valueOfPrefix(prefix);
            var cachedScheduleTypes = scheduleTypeCacheService.getScheduleTypes(telegramId);

            if (cachedScheduleTypes.contains(currentScheduleType)) {
                scheduleTypeCacheService.removeScheduleTypes(telegramId, currentScheduleType, cachedScheduleTypes);
                log.info("User {} selected schedule type \"{}\" for configuration \"{}\"", user.getFirstName(),
                        currentScheduleType.getDescription(), config.getConfigName());
            } else {
                scheduleTypeCacheService.putScheduleTypes(telegramId, currentScheduleType, cachedScheduleTypes);
                log.info("User {} removed selection of schedule type \"{}\" for configuration \"{}\"",
                        user.getFirstName(), currentScheduleType.getDescription(), config.getConfigName());
            }

            return String.format(SELECT_SCHEDULE.getTemplate(), config.getConfigName());
        } else {
            return processConfigNotFoundMessage(user, configId);
        }
    }
}
