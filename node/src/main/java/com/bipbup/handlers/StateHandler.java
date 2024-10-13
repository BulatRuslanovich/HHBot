package com.bipbup.handlers;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;

public interface StateHandler {

    String process(AppUser user, String input);

    AppUserState state();
}
