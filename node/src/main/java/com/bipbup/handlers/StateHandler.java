package com.bipbup.handlers;

import com.bipbup.entity.AppUser;


public interface StateHandler {
    String process(final AppUser user, final String input);
}
