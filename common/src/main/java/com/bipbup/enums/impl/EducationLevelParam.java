package com.bipbup.enums.impl;

import com.bipbup.enums.EnumParam;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EducationLevelParam implements EnumParam {
    NOT_REQUIRED_OR_NOT_SPECIFIED("not_required_or_not_specified",
            "Не требуется или не указано"),
    HIGHER("higher", "Высшее"),
    SECONDARY_VOCATIONAL("special_secondary", "Среднее специальное");

    private final String param;
    private final String description;
}
