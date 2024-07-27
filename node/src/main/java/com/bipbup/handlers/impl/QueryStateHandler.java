package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class QueryStateHandler implements StateHandler {
    private final UserUtil userUtil;

    private static final String UPDATE_COMMAND = "Обновить";
    private static final String DELETE_COMMAND = "Удалить";
    private static final String UPDATE_COMMAND_TEXT = "Введите новый запрос:";

    @Override
    public String process(AppUser appUser, String text) {
        text = text.replace("+", "%2B");

        return switch (text) {
            case UPDATE_COMMAND -> handleUpdateCommand();
            case DELETE_COMMAND -> handleDeleteCommand(appUser);
            default -> handleQueryText(appUser, text);
        };
    }

    private String handleUpdateCommand() {
        return UPDATE_COMMAND_TEXT;
    }

    private String handleDeleteCommand(AppUser appUser) {
        userUtil.updateUserQuery(appUser, null);
        return "Запрос успешно удален.";
    }

    private String handleQueryText(AppUser appUser, String text) {
        if (!isValidQueryText(text)) {
            return "Некорректный запрос. Пожалуйста, проверьте введенные данные.";
        }

        userUtil.updateUserQuery(appUser, text);
        log.info("User {} set query \"{}\"", appUser.getFirstName(), text);
        return "Запрос успешно изменен.";
    }

    private boolean isValidQueryText(String text) {
        return !Objects.isNull(text) && !text.trim().isEmpty() && text.length() <= 50;
    }
}
