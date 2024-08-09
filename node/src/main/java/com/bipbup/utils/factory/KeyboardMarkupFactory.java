package com.bipbup.utils.factory;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Component
public class KeyboardMarkupFactory {
    private static final int BUTTONS_PER_ROW = 2;

    private static final String QUERY_PREFIX = "query_";
    private static final String UPDATE_PREFIX = "update_";
    private static final String DELETE_PREFIX = "delete_";

    private static final String BUTTON_TEXT_UPDATE = "Обновить";
    private static final String BUTTON_TEXT_DELETE = "Удалить";
    private static final String BUTTON_TEXT_BACK = "Назад";
    private static final String BUTTON_TEXT_DELETE_YES = "Да, удалить";
    private static final String BUTTON_TEXT_DELETE_NO = "Нет, не удалять";

    private final AppUserConfigDAO appUserConfigDAO;

    public InlineKeyboardMarkup createUserConfigListKeyboard(AppUser appUser) {
        List<AppUserConfig> appUserConfigs = appUserConfigDAO.findByAppUser(appUser);
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        appUserConfigs.forEach(config ->
                buttons.add(createButton(config.getConfigName(), QUERY_PREFIX + config.getUserConfigId()))
        );

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createConfigManagementKeyboard(CallbackQuery callbackQuery) {
        String configId = extractId(callbackQuery.getData(), QUERY_PREFIX);

        List<InlineKeyboardButton> buttons = List.of(
                createButton(BUTTON_TEXT_UPDATE, UPDATE_PREFIX + configId),
                createButton(BUTTON_TEXT_DELETE, DELETE_PREFIX + configId),
                createButton(BUTTON_TEXT_BACK, "back_to_query_list")
        );

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createDeleteConfirmationKeyboard(CallbackQuery callbackQuery) {
        String configId = extractId(callbackQuery.getData(), DELETE_PREFIX);

        List<InlineKeyboardButton> buttons = List.of(
                createButton(BUTTON_TEXT_DELETE_YES, "delete_yes_" + configId),
                createButton(BUTTON_TEXT_DELETE_NO, "delete_no")
        );

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    private InlineKeyboardButton createButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    private InlineKeyboardMarkup createMarkup(List<InlineKeyboardButton> buttons, int buttonsPerRow) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i += buttonsPerRow) {
            rows.add(buttons.subList(i, Math.min(i + buttonsPerRow, buttons.size())));
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private String extractId(String data, String prefix) {
        return data.substring(prefix.length());
    }
}
