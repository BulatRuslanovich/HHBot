package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.impl.ScheduleTypeParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_FOUND_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.SCHEDULE_FLEXIBLE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.SCHEDULE_FULL_DAY_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.SCHEDULE_REMOTE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.SCHEDULE_SAVE_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.SCHEDULE_SAVE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.SCHEDULE_SHIFT_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.SELECT_SCHEDULE_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.WAIT_SCHEDULE_STATE_PREFIX;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitScheduleStateHandler implements StateHandler {

    private final ConfigService configService;

    private final UserService userService;

    private final Decoder decoder;

    private static final Map<String, ScheduleTypeParam> scheduleTypes = Map.of(
            SCHEDULE_FULL_DAY_PREFIX, ScheduleTypeParam.FULL_DAY,
            SCHEDULE_REMOTE_PREFIX, ScheduleTypeParam.REMOTE_WORKING,
            SCHEDULE_FLEXIBLE_PREFIX, ScheduleTypeParam.FLEXIBLE_SCHEDULE,
            SCHEDULE_SHIFT_PREFIX, ScheduleTypeParam.SHIFT_SCHEDULE
    );

    @Override
    public String process(final AppUser user, final String input) {
        if (hasSavePrefix(input))
            return processSaveScheduleTypesCommand(user, input);
        if (hasSchedulePrefix(input))
            return processSetScheduleTypeCommand(user, input);

        return "";
    }

    private String processSaveScheduleTypesCommand(final AppUser user, final String input) {
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getById(configId);

        if (optionalConfig.isPresent()) {
            var telegramId = user.getTelegramId();
            var config = optionalConfig.get();
            var selectedScheduleTypes = configService.getSelectedScheduleTypes(telegramId);
            config.setScheduleTypes(selectedScheduleTypes.toArray(new ScheduleTypeParam[0]));

            configService.save(config);
            configService.clearScheduleTypeSelections(telegramId);
            userService.clearUserState(telegramId);

            log.info("User {} saved schedule types for configuration {} and state set to BASIC_STATE", user.getFirstName(), config.getConfigName());
            return String.format(SCHEDULE_SAVE_MESSAGE_TEMPLATE, config.getConfigName());
        } else {
            return processConfigNotFoundMessage(user, configId);
        }
    }

    private String processSetScheduleTypeCommand(final AppUser user, final String input) {
        var prefix = input.substring(0, input.lastIndexOf('_') + 1);
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getById(configId);

        if (optionalConfig.isPresent()) {
            var telegramId = user.getTelegramId();
            var config = optionalConfig.get();
            var currentScheduleType = scheduleTypes.get(prefix);
            var selectedScheduleTypes = configService.getSelectedScheduleTypes(telegramId);

            if (selectedScheduleTypes.contains(currentScheduleType)) {
                configService.removeScheduleTypeSelection(telegramId, currentScheduleType, selectedScheduleTypes);
                log.info("User {} selected schedule type \"{}\" for configuration \"{}\"",
                        user.getFirstName(), currentScheduleType.getDescription(), config.getConfigName());
            } else {
                configService.addScheduleTypeSelection(telegramId, currentScheduleType, selectedScheduleTypes);
                log.info("User {} removed selection of schedule type \"{}\" for configuration \"{}\"",
                        user.getFirstName(), currentScheduleType.getDescription(), config.getConfigName());
            }

            return String.format(SELECT_SCHEDULE_MESSAGE_TEMPLATE, config.getConfigName());
        } else {
            return processConfigNotFoundMessage(user, configId);
        }
    }

    private String processConfigNotFoundMessage(final AppUser user, final long configId) {
        userService.clearUserState(user.getTelegramId());
        log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
        return CONFIG_NOT_FOUND_MESSAGE;
    }

    private boolean hasSavePrefix(final String input) {
        return input.startsWith(SCHEDULE_SAVE_PREFIX);
    }

    private boolean hasSchedulePrefix(final String input) {
        return input.startsWith(WAIT_SCHEDULE_STATE_PREFIX);
    }
}
