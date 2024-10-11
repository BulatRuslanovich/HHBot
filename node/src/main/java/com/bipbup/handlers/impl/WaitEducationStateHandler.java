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

import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.EDU_SAVE;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.SELECT_EDUCATION;
import static com.bipbup.utils.CommandMessageConstants.Prefix;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitEducationStateHandler implements StateHandler {

    private final ConfigService configService;

    private final UserService userService;

    private final Decoder decoder;

    @Override
    public String process(AppUser user, String input) {
        if (hasSavePrefix(input))
            return processSaveEducationLevelsCommand(user, input);
        if (hasEducationPrefix(input))
            return processSetEducationLevelCommand(user, input);

        return "";
    }

    private boolean hasSavePrefix(String input) {
        return input.startsWith(Prefix.EDU_SAVE);
    }

    private boolean hasEducationPrefix(String input) {
        return input.startsWith(Prefix.WAIT_EDU_STATE);
    }

    private String processSaveEducationLevelsCommand(AppUser user, String input) {
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
            return String.format(EDU_SAVE.getTemplate(), config.getConfigName());
        } else {
            return processConfigNotFoundMessage(user, configId);
        }
    }

    private String processSetEducationLevelCommand(AppUser user, String input) {
        var prefix = input.substring(0, input.lastIndexOf('_') + 1);
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getById(configId);

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            var currentEducationLevel = EducationLevelParam.valueOfPrefix(prefix);
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

            return String.format(SELECT_EDUCATION.getTemplate(), config.getConfigName());
        } else
            return processConfigNotFoundMessage(user, configId);
    }

    private String processConfigNotFoundMessage(AppUser user, long configId) {
        userService.clearUserState(user.getTelegramId());
        log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
        return CONFIG_NOT_FOUND.getTemplate();
    }
}
