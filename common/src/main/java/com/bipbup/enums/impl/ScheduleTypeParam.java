package com.bipbup.enums.impl;

import com.bipbup.enums.EnumParam;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleTypeParam implements EnumParam {
    FULL_DAY("fullDay", "Полный день", "schedule_full_day_"),
    REMOTE_WORKING("remote", "Удалённая работа", "schedule_remote_"),
    FLEXIBLE_SCHEDULE("flexible", "Гибкий график", "schedule_flexible_"),
    SHIFT_SCHEDULE("shift", "Сменный график", "schedule_shift_");

    private final String param;
    private final String description;
    private final String prefix;
}
