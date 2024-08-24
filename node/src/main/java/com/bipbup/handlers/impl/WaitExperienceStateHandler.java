package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.impl.ExperienceParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.EXP_SET;
import static com.bipbup.utils.CommandMessageConstants.Prefix;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitExperienceStateHandler implements StateHandler {

    private final ConfigService configService;

    private final UserService userService;

    private final Decoder decoder;

    @Override
    public String process(final AppUser user, final String input) {
        if (hasExperiencePrefix(input)) return processSetExperienceCommand(user, input);

        return "";
    }

    private boolean hasExperiencePrefix(String input) {
        return input.startsWith(Prefix.WAIT_EXP_STATE);
    }

    private String processSetExperienceCommand(final AppUser user, final String input) {
        var prefix = input.substring(0, input.lastIndexOf('_') + 1);
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getById(configId);

        userService.clearUserState(user.getTelegramId());

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            var experience = ExperienceParam.valueOfPrefix(prefix);
            config.setExperience(experience);
            configService.save(config);

            log.info("User {} selected experience level and state set to BASIC_STATE", user.getFirstName());
            return String.format(EXP_SET.getTemplate(), experience.getDescription(), config.getConfigName());
        } else {
            userService.clearUserState(user.getTelegramId());
            log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND.getTemplate();
        }
    }
}
