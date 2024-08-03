package com.bipbup.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleTypeParam {
    FULL_DAY("fullDay"),
    REMOTE_WORKING("remote"),
    FLEXIBLE_SCHEDULE("flexible"),
    SHIFT_SCHEDULE("shift");

    private final String param;
}