package com.bipbup.handlers;

import com.bipbup.entity.AppUser;

public interface StateHandler {
    String process(AppUser appUser, String text);
}
