package com.bipbup.handlers.impl.callback;

import com.bipbup.enums.AppUserState;

public record ActionInfo(AppUserState state, String output, boolean saveSelection) {
}
