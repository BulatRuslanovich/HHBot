package com.bipbup.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EducationLevelParam {
    NOT_REQUIRED_OR_NOT_SPECIFIED("not_required_or_not_specified"),
    HIGHER("higher"),
    SECONDARY_VOCATIONAL("special_secondary");

    private final String param;
}
