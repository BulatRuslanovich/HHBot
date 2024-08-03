package com.bipbup.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExperienceParam {
    NO_MATTER("no_matter"),
    NO_EXPERIENCE("noExperience"),
    BETWEEN_1_AND_3("between1And3"),
    BETWEEN_3_AND_6("between3And6"),
    MORE_THEN_6("moreThan6");

    private final String param;
}
