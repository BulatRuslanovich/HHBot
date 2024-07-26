package com.bipbup.handlers.impl;

import com.bipbup.config.KeyboardProperties;
import com.bipbup.entity.AppUser;
import com.bipbup.enums.ExperienceParam;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.bipbup.enums.AppUserState.BASIC_STATE;


@Component
public class ExperienceStateHandler implements StateHandler {
    private final UserUtil userUtil;
    private final KeyboardProperties keyboardProperties;
    private final Map<String, ExperienceParam> experienceMapping;

    public ExperienceStateHandler(UserUtil userUtil, KeyboardProperties keyboardProperties) {
        this.userUtil = userUtil;
        this.keyboardProperties = keyboardProperties;
        this.experienceMapping = new HashMap<>();
        initializeExperienceMapping();
    }

    private void initializeExperienceMapping() {
        experienceMapping.put(keyboardProperties.getNoExperience(), ExperienceParam.NO_EXPERIENCE);
        experienceMapping.put(keyboardProperties.getOneToThreeYears(), ExperienceParam.BETWEEN_1_AND_3);
        experienceMapping.put(keyboardProperties.getThreeToSixYears(), ExperienceParam.BETWEEN_3_AND_6);
        experienceMapping.put(keyboardProperties.getMoreThanSixYears(), ExperienceParam.MORE_THEN_6);
    }

    @Override
    public String process(AppUser appUser, String text) {
        ExperienceParam experienceParam = experienceMapping.getOrDefault(text, ExperienceParam.NO_MATTER);
        appUser.setExperience(experienceParam);
        userUtil.updateUserState(appUser, BASIC_STATE);

        return experienceParam == ExperienceParam.NO_MATTER
                ? "Опыт работы не будет фильтроваться"
                : String.format("Опыт работы успешно установлен (%s)", text);
    }
}
