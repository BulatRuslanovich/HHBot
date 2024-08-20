package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.impl.EducationLevelParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_FOUND_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.EDU_HIGHER_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EDU_NOT_IMPORTANT_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EDU_SAVE_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.EDU_SAVE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EDU_SECONDARY_VOCATIONAL_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.SELECT_EDUCATION_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.WAIT_EDU_STATE_PREFIX;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitEducationStateHandler implements StateHandler {

    private final ConfigService configService;

    private final UserService userService;

    private final Decoder decoder;

    private static final Map<String, EducationLevelParam> educationLevels = Map.of(
            EDU_NOT_IMPORTANT_PREFIX, EducationLevelParam.NOT_REQUIRED_OR_NOT_SPECIFIED,
            EDU_HIGHER_PREFIX, EducationLevelParam.HIGHER,
            EDU_SECONDARY_VOCATIONAL_PREFIX, EducationLevelParam.SECONDARY_VOCATIONAL
    );

    @Override
    public String process(final AppUser user, final String input) {
        if (hasSavePrefix(input)) return processSaveEducationLevelsCommand(user, input);
        if (hasEducationPrefix(input)) return processSetEducationLevelCommand(user, input);

        return "";
    }

    private String processSaveEducationLevelsCommand(final AppUser user, final String input) {
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getById(configId);

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            var selectedEducationLevels = configService.getSelectedEducationLevels(user.getTelegramId());
            config.setEducationLevels(selectedEducationLevels.toArray(new EducationLevelParam[0]));

            configService.save(config);
            configService.clearEducationLevelSelections(user.getTelegramId());
            userService.clearUserState(user.getTelegramId());

            log.info("User {} saved education levels for configuration {} and state set to BASIC_STATE", user.getFirstName(), config.getConfigName());
            return String.format(EDU_SAVE_MESSAGE_TEMPLATE, config.getConfigName());
        } else {
            return processConfigNotFoundMessage(user, configId);
        }
    }

    private String processSetEducationLevelCommand(final AppUser user, final String input) {
        var prefix = input.substring(0, input.lastIndexOf('_') + 1);
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getById(configId);

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            var currentEducationLevel = educationLevels.get(prefix);
            var selectedEducationLevels = configService.getSelectedEducationLevels(user.getTelegramId());

            if (selectedEducationLevels.contains(currentEducationLevel)) {
                configService.removeEducationLevelSelection(user.getTelegramId(), currentEducationLevel, selectedEducationLevels);
                log.info("User {} selected education level \"{}\" for configuration \"{}\"",
                        user.getFirstName(), currentEducationLevel.getDescription(), config.getConfigName());
            } else {
                configService.addEducationLevelSelection(user.getTelegramId(), currentEducationLevel, selectedEducationLevels);
                log.info("User {} removed selection of education level \"{}\" for configuration \"{}\"",
                        user.getFirstName(), currentEducationLevel.getDescription(), config.getConfigName());
            }

            return String.format(SELECT_EDUCATION_MESSAGE_TEMPLATE, config.getConfigName());
        } else
            return processConfigNotFoundMessage(user, configId);
    }

    private String processConfigNotFoundMessage(final AppUser user, final long configId) {
        userService.clearUserState(user.getTelegramId());
        log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
        return CONFIG_NOT_FOUND_MESSAGE;
    }

    private boolean hasSavePrefix(final String input) {
        return input.startsWith(EDU_SAVE_PREFIX);
    }

    private boolean hasEducationPrefix(final String input) {
        return input.startsWith(WAIT_EDU_STATE_PREFIX);
    }
}
