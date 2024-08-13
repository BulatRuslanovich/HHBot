package com.bipbup.utils.factory;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.utils.Encoder;
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

    private final AppUserConfigDAO appUserConfigDAO;

    private final Encoder encoder;

    private static final int BUTTONS_PER_ROW = 2;

    private static final String QUERY_PREFIX = "query_";
    private static final String UPDATE_PREFIX = "action_update_";
    private static final String DELETE_PREFIX = "action_delete_";
    private static final String UPDATE_CONFIG_NAME_PREFIX = "update_config_name_";
    private static final String UPDATE_QUERY_PREFIX = "update_query_";
    private static final String UPDATE_AREA_PREFIX = "update_area_";
    private static final String UPDATE_EXPERIENCE_PREFIX = "update_experience_";
    private static final String UPDATE_EDUCATION_PREFIX = "update_education_";
    private static final String UPDATE_SCHEDULE_PREFIX = "update_schedule_";
    private static final String DELETE_CONFIRM_PREFIX = "delete_confirm_";
    private static final String DELETE_CANCEL_COMMAND = "delete_cancel";
    private static final String BACK_TO_QUERY_LIST_COMMAND = "back_to_query_list";

    private static final String BUTTON_TEXT_UPDATE = "Обновить";
    private static final String BUTTON_TEXT_DELETE = "Удалить";
    private static final String BUTTON_TEXT_BACK = "Назад";
    private static final String BUTTON_TEXT_DELETE_YES = "Да, удалить";
    private static final String BUTTON_TEXT_DELETE_NO = "Нет, не удалять";
    private static final String BUTTON_TEXT_EDIT_CONFIG_NAME = "Изменить название";
    private static final String BUTTON_TEXT_EDIT_QUERY = "Изменить запрос";
    private static final String BUTTON_TEXT_EDIT_AREA = "Изменить регион";
    private static final String BUTTON_TEXT_EDIT_EXPERIENCE = "Изменить опыт работы";
    private static final String BUTTON_TEXT_EDIT_EDUCATION = "Изменить уровень образования";
    private static final String BUTTON_TEXT_EDIT_SCHEDULE = "Изменить график работы";

    public InlineKeyboardMarkup createUserConfigListKeyboard(AppUser appUser) {
        var userConfigs = appUserConfigDAO.findByAppUser(appUser);
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        userConfigs.forEach(c -> buttons.add(createButton(c.getConfigName(),
                String.format("%s%s", QUERY_PREFIX, encoder.hashOf(c.getUserConfigId())))
        ));

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createConfigManagementKeyboard(CallbackQuery callbackQuery) {
        var hash = extractHash(callbackQuery.getData(), QUERY_PREFIX);

        List<InlineKeyboardButton> buttons = List.of(
                createButton(BUTTON_TEXT_UPDATE, UPDATE_PREFIX + hash),
                createButton(BUTTON_TEXT_DELETE, DELETE_PREFIX + hash),
                createButton(BUTTON_TEXT_BACK, BACK_TO_QUERY_LIST_COMMAND)
        );

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createDeleteConfirmationKeyboard(CallbackQuery callbackQuery) {
        var hash = extractHash(callbackQuery.getData(), DELETE_PREFIX);

        List<InlineKeyboardButton> buttons = List.of(
                createButton(BUTTON_TEXT_DELETE_YES, DELETE_CONFIRM_PREFIX + hash),
                createButton(BUTTON_TEXT_DELETE_NO, DELETE_CANCEL_COMMAND)
        );

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createUpdateConfigKeyboard(CallbackQuery callbackQuery) {
        var hash = extractHash(callbackQuery.getData(), UPDATE_PREFIX);

        List<InlineKeyboardButton> buttons = List.of(
                createButton(BUTTON_TEXT_EDIT_CONFIG_NAME, UPDATE_CONFIG_NAME_PREFIX + hash),
                createButton(BUTTON_TEXT_EDIT_QUERY, UPDATE_QUERY_PREFIX + hash),
                createButton(BUTTON_TEXT_EDIT_AREA, UPDATE_AREA_PREFIX + hash),
                createButton(BUTTON_TEXT_EDIT_EXPERIENCE, UPDATE_EXPERIENCE_PREFIX + hash),
                createButton(BUTTON_TEXT_EDIT_EDUCATION, UPDATE_EDUCATION_PREFIX + hash),
                createButton(BUTTON_TEXT_EDIT_SCHEDULE, UPDATE_SCHEDULE_PREFIX + hash)
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

    private String extractHash(String data, String prefix) {
        return data.substring(prefix.length());
    }
}
