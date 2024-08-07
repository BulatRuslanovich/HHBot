package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserConfigUtil;
import com.bipbup.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.BASIC_STATE;

@RequiredArgsConstructor
@Component
public class QueryDeleteStateHandler implements StateHandler {
    private final UserUtil userUtil;
    private final UserConfigUtil userConfigUtil;

    @Override
    public String process(AppUser appUser, String text) {
        if (text.equals("/cancel")) {
            userUtil.updateUserState(appUser, BASIC_STATE);
            return "Команда отменена!";
        }

        if (text.startsWith("delete_yes_")) {
            long configId = Long.parseLong(text.substring("delete_yes_".length()));
            var optional = userConfigUtil.getConfigById(configId);

            if (optional.isPresent()) {
                appUser.setState(BASIC_STATE);
                userConfigUtil.removeConfig(optional.get());
                return "Конфигурация была удалена.";
            }
        }

        if (text.equals("delete_no")) {
            userUtil.updateUserState(appUser, BASIC_STATE);
            return "Конфигурация не была удалена.";
        }

        return "";
    }
}
