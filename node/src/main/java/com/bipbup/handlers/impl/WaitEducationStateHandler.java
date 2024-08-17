package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.impl.EducationLevelParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
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
@Component
public class WaitEducationStateHandler implements StateHandler {
    ConfigService configService;

    UserService userService;

    Decoder decoder;

    private final Map<String, EducationLevelParam> educationLevels;

    public WaitEducationStateHandler(final ConfigService configService,
                                     UserService userService,
                                     final Decoder decoder) {
        this.configService = configService;
        this.userService = userService;
        this.decoder = decoder;

        this.educationLevels = Map.of(
                EDU_NOT_IMPORTANT_PREFIX, EducationLevelParam.NOT_REQUIRED_OR_NOT_SPECIFIED,
                EDU_HIGHER_PREFIX, EducationLevelParam.HIGHER,
                EDU_SECONDARY_VOCATIONAL_PREFIX, EducationLevelParam.SECONDARY_VOCATIONAL
        );
    }

    @Override
    public String process(AppUser user, String input) {
        if (hasSavePrefix(input)) return processSaveEducationLevelsCommand(user, input);
        if (hasEducationPrefix(input)) return processSetEducationLevelCommand(user, input);

        return "";
    }

    private String processSaveEducationLevelsCommand(AppUser user, String input) {
        var configId = decoder.getIdByCallback(input);
        var configOptional = configService.getById(configId);

        if (configOptional.isPresent()) {
            var config = configOptional.get();
            var selectedEducationLevels = configService.getSelectedEducationLevels(user.getTelegramId());
            config.setEducationLevels(selectedEducationLevels.toArray(new EducationLevelParam[0]));

            configService.save(config);
            configService.clearEducationLevelSelections(user.getTelegramId());
            userService.clearUserState(user.getTelegramId());

            log.info("User {} saved education levels for configuration {} and state set to BASIC_STATE", user.getFirstName(), config.getConfigName());
            return String.format(EDU_SAVE_MESSAGE_TEMPLATE, config.getConfigName());
        } else
            return processConfigNotFoundMessage(user, configId);
    }

    private String processSetEducationLevelCommand(AppUser user, String input) {
        var prefix = input.substring(0, input.lastIndexOf('_') + 1);
        var configId = decoder.getIdByCallback(input);
        var configOptional = configService.getById(configId);

        if (configOptional.isPresent()) {
            var config = configOptional.get();
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

    private String processConfigNotFoundMessage(final AppUser user, long configId) {
        userService.clearUserState(user.getTelegramId());
        log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
        return CONFIG_NOT_FOUND_MESSAGE;
    }

    private boolean hasSavePrefix(String input) {
        return input.startsWith(EDU_SAVE_PREFIX);
    }

    private boolean hasEducationPrefix(String input) {
        return input.startsWith(WAIT_EDU_STATE_PREFIX);
    }
}
