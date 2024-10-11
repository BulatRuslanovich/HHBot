package com.bipbup.handlers.impl;

import com.bipbup.enums.AppUserState;

public record ActionProperties(AppUserState state, String output, boolean saveSelection) {
}
