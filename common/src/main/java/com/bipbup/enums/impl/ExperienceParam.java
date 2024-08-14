package com.bipbup.enums.impl;

import com.bipbup.enums.EnumParam;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExperienceParam implements EnumParam {
    NO_MATTER("noMatter", "Не имеет значения"),
    NO_EXPERIENCE("noExperience", "Нет опыта"),
    BETWEEN_1_AND_3("between1And3", "От 1 года до 3 лет"),
    BETWEEN_3_AND_6("between3And6", "От 3 до 6 лет"),
    MORE_THAN_6("moreThan6", "Более 6 лет");

    private final String param;
    private final String description;
}
