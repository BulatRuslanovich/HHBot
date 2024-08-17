package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.impl.ScheduleTypeParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.bipbup.utils.CommandMessageConstants.*;

@Slf4j
@Component
public class WaitScheduleStateHandler implements StateHandler {
    ConfigService configService;

    UserService userService;

    Decoder decoder;

    private final Map<String, ScheduleTypeParam> scheduleTypes;

    public WaitScheduleStateHandler(final ConfigService configService,
                                     UserService userService,
                                     final Decoder decoder) {
        this.configService = configService;
        this.userService = userService;
        this.decoder = decoder;

        this.scheduleTypes = Map.of(
                SCHEDULE_FULL_DAY_PREFIX, ScheduleTypeParam.FULL_DAY,
                SCHEDULE_REMOTE_PREFIX, ScheduleTypeParam.REMOTE_WORKING,
                SCHEDULE_FLEXIBLE_PREFIX, ScheduleTypeParam.FLEXIBLE_SCHEDULE,
                SCHEDULE_SHIFT_PREFIX, ScheduleTypeParam.SHIFT_SCHEDULE
        );
    }

    @Override
    public String process(AppUser user, String input) {
        if (hasSavePrefix(input)) return processSaveScheduleTypesCommand(user, input);
        if (hasSchedulePrefix(input)) return processSetScheduleTypeCommand(user, input);

        return "";
    }

    private String processSaveScheduleTypesCommand(AppUser user, String input) {
        var configId = decoder.getIdByCallback(input);
        var configOptional = configService.getById(configId);

        if (configOptional.isPresent()) {
            var config = configOptional.get();
            var selectedScheduleTypes = configService.getSelectedScheduleTypes(user.getTelegramId());
            config.setScheduleTypes(selectedScheduleTypes.toArray(new ScheduleTypeParam[0]));

            configService.save(config);
            configService.clearScheduleTypeSelections(user.getTelegramId());
            userService.clearUserState(user.getTelegramId());

            log.info("User {} saved schedule types for configuration {} and state set to BASIC_STATE", user.getFirstName(), config.getConfigName());
            return String.format(SCHEDULE_SAVE_MESSAGE_TEMPLATE, config.getConfigName());
        } else
            return processConfigNotFoundMessage(user, configId);
    }

    private String processSetScheduleTypeCommand(AppUser user, String input) {
        var prefix = input.substring(0, input.lastIndexOf('_') + 1);
        var configId = decoder.getIdByCallback(input);
        var configOptional = configService.getById(configId);

        if (configOptional.isPresent()) {
            var config = configOptional.get();
            var currentScheduleType = scheduleTypes.get(prefix);
            var selectedScheduleTypes = configService.getSelectedScheduleTypes(user.getTelegramId());

            if (selectedScheduleTypes.contains(currentScheduleType)) {
                configService.removeScheduleTypeSelection(user.getTelegramId(), currentScheduleType, selectedScheduleTypes);
                log.info("User {} selected schedule type \"{}\" for configuration \"{}\"",
                        user.getFirstName(), currentScheduleType.getDescription(), config.getConfigName());
            } else {
                configService.addScheduleTypeSelection(user.getTelegramId(), currentScheduleType, selectedScheduleTypes);
                log.info("User {} removed selection of schedule type \"{}\" for configuration \"{}\"",
                        user.getFirstName(), currentScheduleType.getDescription(), config.getConfigName());
            }

            return String.format(SELECT_SCHEDULE_MESSAGE_TEMPLATE, config.getConfigName());
        } else
            return processConfigNotFoundMessage(user, configId);
    }

    private String processConfigNotFoundMessage(final AppUser user, long configId) {
        userService.clearUserState(user.getTelegramId());
        log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
        return CONFIG_NOT_FOUND_MESSAGE;
    }

    private boolean hasSavePrefix(String input) {
        return input.startsWith(SCHEDULE_SAVE_PREFIX);
    }

    private boolean hasSchedulePrefix(String input) {
        return input.startsWith(WAIT_SCHEDULE_STATE_PREFIX);
    }
}
