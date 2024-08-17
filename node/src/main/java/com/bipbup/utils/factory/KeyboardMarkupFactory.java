package com.bipbup.utils.factory;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.impl.EducationLevelParam;
import com.bipbup.service.ConfigService;
import com.bipbup.utils.Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_BACK;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_DELETE;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_DELETE_CANCEL;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_DELETE_CONFIRM;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_EDU_HIGHER;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_EDU_NOT_IMPORTANT;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_EDU_SECONDARY_VOCATIONAL;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_EXP_1_3_YEARS;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_EXP_3_6_YEARS;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_EXP_MORE_6_YEARS;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_EXP_NOT_IMPORTANT;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_NO_EXP;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_SAVE;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_SELECTED;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_UPDATE;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_UPDATE_AREA;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_UPDATE_CONFIG_NAME;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_UPDATE_EDUCATION;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_UPDATE_EXPERIENCE;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_UPDATE_QUERY;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_UPDATE_SCHEDULE;
import static com.bipbup.utils.CommandMessageConstants.DELETE_CANCEL_COMMAND;
import static com.bipbup.utils.CommandMessageConstants.DELETE_CONFIRM_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.DELETE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EDU_HIGHER_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EDU_NOT_IMPORTANT_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EDU_SAVE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EDU_SECONDARY_VOCATIONAL_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EXP_1_3_YEARS_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EXP_3_6_YEARS_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EXP_MORE_6_YEARS_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.EXP_NOT_IMPORTANT_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.MYQUERIES_COMMAND;
import static com.bipbup.utils.CommandMessageConstants.NO_EXP_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.QUERY_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_AREA_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_CONFIG_NAME_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_EDUCATION_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_EXPERIENCE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_QUERY_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_SCHEDULE_PREFIX;

@RequiredArgsConstructor
@Component
public class KeyboardMarkupFactory {
    private final ConfigService configService;

    private final Encoder encoder;

    private static final int BUTTONS_PER_ROW = 2;

    public InlineKeyboardMarkup createUserConfigListKeyboard(AppUser appUser) {
        var userConfigs = configService.getByUser(appUser);
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        userConfigs.forEach(c -> buttons.add(createButton(c.getConfigName(),
                String.format("%s%s", QUERY_PREFIX, encoder.hashOf(c.getUserConfigId())))
        ));

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createConfigManagementKeyboard(String callbackData) {
        var hash = extractHash(callbackData);

        List<InlineKeyboardButton> buttons = List.of(
                createButton(BUTTON_TEXT_UPDATE, UPDATE_PREFIX + hash),
                createButton(BUTTON_TEXT_DELETE, DELETE_PREFIX + hash),
                createButton(BUTTON_TEXT_BACK, MYQUERIES_COMMAND)
        );

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createDeleteConfirmationKeyboard(String callbackData) {
        var hash = extractHash(callbackData);

        List<InlineKeyboardButton> buttons = List.of(
                createButton(BUTTON_TEXT_DELETE_CONFIRM, DELETE_CONFIRM_PREFIX + hash),
                createButton(BUTTON_TEXT_DELETE_CANCEL, DELETE_CANCEL_COMMAND)
        );

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createUpdateConfigKeyboard(String callbackData) {
        var hash = extractHash(callbackData);

        List<InlineKeyboardButton> buttons = List.of(
                createButton(BUTTON_TEXT_UPDATE_CONFIG_NAME, UPDATE_CONFIG_NAME_PREFIX + hash),
                createButton(BUTTON_TEXT_UPDATE_QUERY, UPDATE_QUERY_PREFIX + hash),
                createButton(BUTTON_TEXT_UPDATE_AREA, UPDATE_AREA_PREFIX + hash),
                createButton(BUTTON_TEXT_UPDATE_EXPERIENCE, UPDATE_EXPERIENCE_PREFIX + hash),
                createButton(BUTTON_TEXT_UPDATE_EDUCATION, UPDATE_EDUCATION_PREFIX + hash),
                createButton(BUTTON_TEXT_UPDATE_SCHEDULE, UPDATE_SCHEDULE_PREFIX + hash),
                createButton(BUTTON_TEXT_BACK, QUERY_PREFIX + hash)
        );

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createExperienceSelectionKeyboard(String callbackData) {
        var hash = extractHash(callbackData);

        List<InlineKeyboardButton> buttons = List.of(
                createButton(BUTTON_TEXT_NO_EXP, NO_EXP_PREFIX + hash),
                createButton(BUTTON_TEXT_EXP_1_3_YEARS, EXP_1_3_YEARS_PREFIX + hash),
                createButton(BUTTON_TEXT_EXP_3_6_YEARS, EXP_3_6_YEARS_PREFIX + hash),
                createButton(BUTTON_TEXT_EXP_MORE_6_YEARS, EXP_MORE_6_YEARS_PREFIX + hash),
                createButton(BUTTON_TEXT_EXP_NOT_IMPORTANT, EXP_NOT_IMPORTANT_PREFIX + hash)
        );

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createEducationLevelSelectionKeyboard(AppUser user, String callbackData) {
        List<EducationLevelParam> selectedEducationLevels = configService.getSelectedEducationLevels(user.getTelegramId());

        var hash = extractHash(callbackData);

        var buttonTexts = new HashMap<String, String>();
        buttonTexts.put(EDU_NOT_IMPORTANT_PREFIX, BUTTON_TEXT_EDU_NOT_IMPORTANT);
        buttonTexts.put(EDU_HIGHER_PREFIX, BUTTON_TEXT_EDU_HIGHER);
        buttonTexts.put(EDU_SECONDARY_VOCATIONAL_PREFIX, BUTTON_TEXT_EDU_SECONDARY_VOCATIONAL);

        var enumPrefixes = Map.of(
                EducationLevelParam.NOT_REQUIRED_OR_NOT_SPECIFIED, EDU_NOT_IMPORTANT_PREFIX,
                EducationLevelParam.HIGHER, EDU_HIGHER_PREFIX,
                EducationLevelParam.SECONDARY_VOCATIONAL, EDU_SECONDARY_VOCATIONAL_PREFIX
        );

        selectedEducationLevels.forEach(l -> buttonTexts.put(enumPrefixes.get(l), buttonTexts.get(enumPrefixes.get(l)) + BUTTON_TEXT_SELECTED));

        List<InlineKeyboardButton> buttons = List.of(
                createButton(buttonTexts.get(EDU_NOT_IMPORTANT_PREFIX), EDU_NOT_IMPORTANT_PREFIX + hash),
                createButton(buttonTexts.get(EDU_HIGHER_PREFIX), EDU_HIGHER_PREFIX + hash),
                createButton(buttonTexts.get(EDU_SECONDARY_VOCATIONAL_PREFIX), EDU_SECONDARY_VOCATIONAL_PREFIX + hash),
                createButton(BUTTON_TEXT_SAVE, EDU_SAVE_PREFIX + hash)
        );

        return createMarkup(buttons, 1);
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

    private String extractHash(String data) {
        return data.substring(data.lastIndexOf("_") + 1);
    }
}
