package com.bipbup.enums;

public enum ScheduleTypeParam {
    FULL_DAY("fullDay"),
    REMOTE_WORKING("remote"),
    FLEXIBLE_SCHEDULE("flexible"),
    SHIFT_SCHEDULE("shift");

    private final String param;

    ScheduleTypeParam(String param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return param;
    }
}