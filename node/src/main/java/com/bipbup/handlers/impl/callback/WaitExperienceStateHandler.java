package com.bipbup.handlers.impl.callback;

import com.bipbup.annotation.CallbackQualifier;
import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import static com.bipbup.enums.AppUserState.WAIT_EXPERIENCE_STATE;
import com.bipbup.enums.impl.ExperienceParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.service.db.ConfigService;
import com.bipbup.service.cache.UserStateCacheService;
import com.bipbup.utils.Decoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.CONFIG_NOT_FOUND;
import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.EXP_SET;
import static com.bipbup.utils.CommandMessageConstants.Prefix;

@Slf4j
@Component
@CallbackQualifier
@RequiredArgsConstructor
public class WaitExperienceStateHandler implements StateHandler {

    private final ConfigService configService;

    private final UserStateCacheService userStateCacheService;

    private final Decoder decoder;

    @Override
    public String process(AppUser user, String input) {
        if (hasExperiencePrefix(input))
            return processSetExperienceCommand(user, input);

        return "";
    }

    @Override
    public AppUserState state() {
        return WAIT_EXPERIENCE_STATE;
    }

    private boolean hasExperiencePrefix(String input) {
        return input.startsWith(Prefix.WAIT_EXP_STATE);
    }

    private String processSetExperienceCommand(AppUser user, String input) {
        var prefix = input.substring(0, input.lastIndexOf('_') + 1);
        var configId = decoder.parseIdFromCallback(input);
        var optionalConfig = configService.getConfigById(configId);

        userStateCacheService.clearUserState(user.getTelegramId());

        if (optionalConfig.isPresent()) {
            var config = optionalConfig.get();
            var experience = ExperienceParam.valueOfPrefix(prefix);
            config.setExperience(experience);
            configService.saveConfig(config);

            log.info("User {} selected experience level and state set to BASIC_STATE", user.getFirstName());
            return String.format(EXP_SET.toString(), experience.getDescription(), config.getConfigName());
        } else {
            userStateCacheService.clearUserState(user.getTelegramId());
            log.debug("Configuration with id {} not found for user {}", configId, user.getFirstName());
            return CONFIG_NOT_FOUND.toString();
        }
    }
}
