package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.handlers.StateHandler;
import com.bipbup.utils.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

import static com.bipbup.enums.AppUserState.*;

@RequiredArgsConstructor
@Component
public class BasicStateHandler implements StateHandler {
    private final UserUtil userUtil;

    @Override
    public String process(AppUser appUser, String text) {
        return switch (text) {
            case "/start" -> startInteraction(appUser);
            case "/myqueries" -> showQueriesOutput(appUser);
            case "/newquery" -> addQueryOutput(appUser);
//            case "/set_experience" -> chooseExpOutput(appUser);
            default -> "";
        };
    }

    private String startInteraction(AppUser appUser) {
        return "Добро пожаловать в капитализм, %s!".formatted(appUser.getFirstName());
    }

    private String addQueryOutput(AppUser appUser) {
        userUtil.updateUserState(appUser, WAIT_QUERY_STATE);
        return "Введите ваш запрос для поиска:";
    }

//    private String chooseExpOutput(AppUser appUser) {
//        userUtil.updateUserState(appUser, WAIT_EXPERIENCE_STATE);
//        return "Выберите опыт работы:";
//    }

    private String showQueriesOutput(AppUser appUser) {
        var appUserConfigs = appUser.getAppUserConfigs();
        if (appUserConfigs == null || appUserConfigs.isEmpty()) {
            return "У вас пока нет сохранённых запросов.\n" +
                    "Введите /newquery, чтобы добавить новый запрос.";
        }

        userUtil.updateUserState(appUser, WAIT_QUERY_SELECTION_STATE);
        return "Ваши запросы:";
    }
}
