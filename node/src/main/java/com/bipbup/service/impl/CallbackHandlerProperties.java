package com.bipbup.service.impl;

import com.bipbup.enums.AppUserState;
import com.bipbup.handlers.StateHandler;

public record CallbackHandlerProperties(AppUserState state, StateHandler handler, String prefix) {
}
