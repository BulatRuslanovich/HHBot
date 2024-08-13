package com.bipbup.enums.impl;

import com.bipbup.enums.EnumParam;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleTypeParam implements EnumParam {
    FULL_DAY("fullDay", "Полный день"),
    REMOTE_WORKING("remote", "Удалённая работа"),
    FLEXIBLE_SCHEDULE("flexible", "Гибкий график"),
    SHIFT_SCHEDULE("shift", "Сменный график");

    private final String param;
    private final String description;
}
