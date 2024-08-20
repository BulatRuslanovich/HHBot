package com.bipbup.enums.impl;

import com.bipbup.enums.EnumParam;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ExperienceParam implements EnumParam {
    NO_MATTER("noMatter", "Не имеет значения", "exp_not_important_"),
    NO_EXPERIENCE("noExperience", "Нет опыта", "exp_no_"),
    BETWEEN_1_AND_3("between1And3", "От 1 года до 3 лет", "exp_1_3_years_"),
    BETWEEN_3_AND_6("between3And6", "От 3 до 6 лет", "exp_3_6_years_"),
    MORE_THAN_6("moreThan6", "Более 6 лет", "exp_more_6_years_");

    private final String param;

    private final String description;

    private final String prefix;

    public static ExperienceParam valueOfPrefix(final String prefix) {
        return Arrays.stream(values())
                .filter(e -> e.prefix.equals(prefix))
                .findFirst().orElseThrow();
    }
}
