package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.bipbup.enums.AppUserState.WAIT_EXPERIENCE_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;

@RequiredArgsConstructor
@Component
public class BasicStateHandler implements StateHandler {
    private final UserUtil userUtil;

    @Override
    public String process(AppUser appUser, String text) {
        return switch (text) {
            case "/start" -> startInteraction(appUser);
            case "/set_query" -> chooseQueryOutput(appUser);
            case "/set_experience" -> chooseExpOutput(appUser);
            default -> "";
        };
    }

    private String startInteraction(AppUser appUser) {
        return "Добро пожаловать в капитализм, %s!".formatted(appUser.getFirstName());
    }

    private String chooseQueryOutput(AppUser appUser) {
        userUtil.updateUserState(appUser, WAIT_QUERY_STATE);
        return "Введите запрос";
    }

    private String chooseExpOutput(AppUser appUser) {
        userUtil.updateUserState(appUser, WAIT_EXPERIENCE_STATE);
        return "Выберите опыт работы";
    }
}
