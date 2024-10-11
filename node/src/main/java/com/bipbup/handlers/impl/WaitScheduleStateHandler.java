package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.ScheduleParamEntity;
import com.bipbup.enums.impl.ScheduleTypeParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.SCHEDULE_SAVE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.SELECT_SCHEDULE;
import static com.bipbup.utils.CommandMessageConstants.Prefix;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitScheduleStateHandler implements StateHandler {

    private final ConfigService configService;

    private final UserService userService;

    private final Decoder decoder;
    
    @Override
    public String process(AppUser user, String input) {
        if (hasSavePrefix(input))
            return processSaveScheduleTypesCommand(user, input);
        if (hasSchedulePrefix(input))
            return processSetScheduleTypeCommand(user, input);

        return "";
    }

    private boolean hasSavePrefix(String input) {
        return input.startsWith(Prefix.SCHEDULE_SAVE);
    }

    private boolean hasSchedulePrefix(String input) {
        return input.startsWith(Prefix.WAIT_SCHEDULE_STATE);
    }

    private String processConfigNotFoundMessage(AppUser user, long configId) {
        userService.clearUserState(user.getTelegramId());
        log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
        return CONFIG_NOT_FOUND.getTemplate();
    }

    private String processSaveScheduleTypesCommand(AppUser user, String input) {
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getById(configId);

        if (optionalConfig.isPresent()) {
            var telegramId = user.getTelegramId();
            var config = optionalConfig.get();
            var selectedScheduleTypes = configService.getSelectedScheduleTypes(telegramId);
            var listOfScheduleTypeEntities = selectedScheduleTypes.stream()
                    .map(s -> new ScheduleParamEntity(null, s, config)).toList();
            config.setScheduleParams(listOfScheduleTypeEntities);

            configService.save(config);
            configService.clearScheduleTypeSelections(telegramId);
            userService.clearUserState(telegramId);

            log.info("User {} saved schedule types for configuration {} and state set to BASIC_STATE", user.getFirstName(), config.getConfigName());
            return String.format(SCHEDULE_SAVE.getTemplate(), config.getConfigName());
        } else {
            return processConfigNotFoundMessage(user, configId);
        }
    }

    private String processSetScheduleTypeCommand(AppUser user, String input) {
        var prefix = input.substring(0, input.lastIndexOf('_') + 1);
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getById(configId);

        if (optionalConfig.isPresent()) {
            var telegramId = user.getTelegramId();
            var config = optionalConfig.get();
            var currentScheduleType = ScheduleTypeParam.valueOfPrefix(prefix);
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

            return String.format(SELECT_SCHEDULE.getTemplate(), config.getConfigName());
        } else {
            return processConfigNotFoundMessage(user, configId);
        }
    }
}
