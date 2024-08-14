package com.bipbup.enums.impl;

import com.bipbup.enums.EnumParam;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExperienceParam implements EnumParam {
    NO_MATTER("no_matter", "Не имеет значения"),
    NO_EXPERIENCE("noExperience", "Нет опыта"),
    BETWEEN_1_AND_3("between1And3", "1-3 года"),
    BETWEEN_3_AND_6("between3And6", "3-6 лет"),
    MORE_THAN_6("moreThan6", "Более 6 лет");

    private final String param;
    private final String description;
}
