package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.impl.ExperienceParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import com.bipbup.utils.Decoder;
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
@Component
public class WaitExperienceStateHandler implements StateHandler {
    ConfigService configService;

    UserService userService;

    Decoder decoder;

    private final Map<String, ExperienceParam> experiences;

    public WaitExperienceStateHandler(ConfigService configService,
                                      UserService userService,
                                      Decoder decoder) {
        this.configService = configService;
        this.userService = userService;
        this.decoder = decoder;

        this.experiences = Map.of(
                EXP_NOT_IMPORTANT_PREFIX, ExperienceParam.NO_MATTER,
                NO_EXP_PREFIX, ExperienceParam.NO_EXPERIENCE,
                EXP_1_3_YEARS_PREFIX, ExperienceParam.BETWEEN_1_AND_3,
                EXP_3_6_YEARS_PREFIX, ExperienceParam.BETWEEN_3_AND_6,
                EXP_MORE_6_YEARS_PREFIX, ExperienceParam.MORE_THAN_6
        );
    }

    @Override
    public String process(AppUser user, String input) {
        if (hasExperiencePrefix(input)) return processSetExperienceCommand(user, input);

        return "";
    }

    private String processSetExperienceCommand(AppUser user, String input) {
        var prefix = input.substring(0, input.lastIndexOf('_') + 1);
        var hash = input.substring(prefix.length());
        var configId = decoder.idOf(hash);
        var configOptional = configService.getById(configId);

        if (configOptional.isPresent()) {
            var config = configOptional.get();
            var experience = experiences.get(prefix);
            config.setExperience(experience);

            configService.save(config);
            userService.clearUserState(user.getTelegramId());

            log.debug("User {} selected experience level and state set to BASIC_STATE", user.getFirstName());
            return String.format(EXP_SET_MESSAGE_TEMPLATE, experience.getDescription(), config.getConfigName());
        } else {
            userService.clearUserState(user.getTelegramId());
            log.warn("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND_MESSAGE;
        }
    }

    private boolean hasExperiencePrefix(String input) {
        return input.startsWith(WAIT_EXP_STATE_PREFIX);
    }
}
