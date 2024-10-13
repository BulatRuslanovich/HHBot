package com.bipbup.handlers.impl.callback;

import com.bipbup.enums.AppUserState;

public record ActionProperties(AppUserState state, String output, boolean saveSelection) {
}
