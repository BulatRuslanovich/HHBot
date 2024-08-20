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

import java.util.Map;

import static com.bipbup.utils.CommandMessageConstants.CONFIG_NOT_FOUND_MESSAGE;
import static com.bipbup.utils.CommandMessageConstants.EXP_1_3_YEARS_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EXP_3_6_YEARS_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EXP_MORE_6_YEARS_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EXP_NOT_IMPORTANT_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EXP_SET_MESSAGE_TEMPLATE;
import static com.bipbup.utils.CommandMessageConstants.NO_EXP_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.WAIT_EXP_STATE_PREFIX;

@Slf4j
@RequiredArgsConstructor
@Component
public class WaitExperienceStateHandler implements StateHandler {

    private final ConfigService configService;

    private final UserService userService;

    private final Decoder decoder;

    private static final Map<String, ExperienceParam> experiences = Map.of(
            EXP_NOT_IMPORTANT_PREFIX, ExperienceParam.NO_MATTER,
            NO_EXP_PREFIX, ExperienceParam.NO_EXPERIENCE,
            EXP_1_3_YEARS_PREFIX, ExperienceParam.BETWEEN_1_AND_3,
            EXP_3_6_YEARS_PREFIX, ExperienceParam.BETWEEN_3_AND_6,
            EXP_MORE_6_YEARS_PREFIX, ExperienceParam.MORE_THAN_6
    );

    @Override
    public String process(final AppUser user, final String input) {
        if (hasExperiencePrefix(input)) return processSetExperienceCommand(user, input);

        return "";
    }

    private String processSetExperienceCommand(final AppUser user, final String input) {
        var prefix = input.substring(0, input.lastIndexOf('_') + 1);
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getById(configId);

        userService.clearUserState(user.getTelegramId());

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            var experience = experiences.get(prefix);
            config.setExperience(experience);
            configService.save(config);

            log.info("User {} selected experience level and state set to BASIC_STATE", user.getFirstName());
            return String.format(EXP_SET_MESSAGE_TEMPLATE, experience.getDescription(), config.getConfigName());
        } else {
            userService.clearUserState(user.getTelegramId());
            log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND_MESSAGE;
        }
    }

    private boolean hasExperiencePrefix(String input) {
        return input.startsWith(WAIT_EXP_STATE_PREFIX);
    }
}
