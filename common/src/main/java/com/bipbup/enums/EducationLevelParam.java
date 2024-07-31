package com.bipbup.enums;

public enum EducationLevelParam {
    NOT_REQUIRED_OR_NOT_SPECIFIED("not_required_or_not_specified"),
    HIGHER("higher"),
    SECONDARY_VOCATIONAL("special_secondary");

    EducationLevelParam(String param) {
        this.param = param;
    }

    private final String param;

    @Override
    public String toString() {
        return param;
    }
}
