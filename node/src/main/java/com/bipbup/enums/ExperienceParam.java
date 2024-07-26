package com.bipbup.enums;

public enum ExperienceParam {
    NO_MATTER("no_matter"),
    NO_EXPERIENCE("noExperience"),
    BETWEEN_1_AND_3("between1And3"),
    BETWEEN_3_AND_6("between3And6"),
    MORE_THEN_6("moreThan6");

    ExperienceParam(String param) {
        this.param = param;
    }

    private final String param;

    @Override
    public String toString() {
        return param;
    }

    public static ExperienceParam fromValue(String value) {
        for (var exp : ExperienceParam.values()) {
            if (exp.param.equals(value)) {
                return exp;
            }
        }

        return null;
    }
}
