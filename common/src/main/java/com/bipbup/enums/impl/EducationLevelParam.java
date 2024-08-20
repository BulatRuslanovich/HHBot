package com.bipbup.enums.impl;

import com.bipbup.enums.EnumParam;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum EducationLevelParam implements EnumParam {
    NOT_REQUIRED_OR_NOT_SPECIFIED("not_required_or_not_specified", "Не требуется или не указано", "edu_not_important_"),
    HIGHER("higher", "Высшее", "edu_higher_"),
    SECONDARY_VOCATIONAL("special_secondary", "Среднее специальное", "edu_special_secondary_");

    private final String param;

    private final String description;

    private final String prefix;

    public static EducationLevelParam valueOfPrefix(final String prefix) {
        return Arrays.stream(values())
                .filter(e -> e.prefix.equals(prefix))
                .findFirst().orElseThrow();
    }
}
