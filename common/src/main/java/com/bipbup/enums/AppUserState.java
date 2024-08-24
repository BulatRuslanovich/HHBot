package com.bipbup.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppUserState {

    BASIC_STATE(false),

    WAIT_CONFIG_NAME_STATE(true),

    WAIT_QUERY_STATE(true),

    QUERY_LIST_STATE(false),

    QUERY_MENU_STATE(false),

    QUERY_DELETE_STATE(false),

    QUERY_UPDATE_STATE(false),

    WAIT_AREA_STATE(true),

    WAIT_EXPERIENCE_STATE(true),

    WAIT_EDUCATION_STATE(true),

    WAIT_SCHEDULE_STATE(true);

    private final boolean isWaiting;
}
