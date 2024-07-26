package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Log4j
@RequiredArgsConstructor
@Component
public class QueryStateHandler implements StateHandler {
    private final UserUtil userUtil;

    @Override
    public String process(AppUser appUser, String text) {
        text = text.replace("+", "%2B");

        if (!isValidQueryText(text)) {
            return "Некорректный запрос. Пожалуйста, проверьте введенные данные.";
        }

        userUtil.updateUserQuery(appUser, text);

        log.info("User %s set query \"%s\"".formatted(appUser.getUsername(), text));
        return "Запрос успешно изменен.";
    }


    private boolean isValidQueryText(String text) {
        return !Objects.isNull(text) && !text.trim().isEmpty() && text.length() <= 50;
    }
}
