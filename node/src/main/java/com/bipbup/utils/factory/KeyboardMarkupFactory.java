package com.bipbup.utils.factory;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.EnumParam;
import com.bipbup.enums.impl.EducationLevelParam;
import com.bipbup.enums.impl.ExperienceParam;
import com.bipbup.enums.impl.ScheduleTypeParam;
import com.bipbup.service.ConfigService;
import com.bipbup.utils.Encoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_BACK;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_DELETE;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_DELETE_CANCEL;
import static com.bipbup.utils.CommandMessageConstants.BUTTON_TEXT_DELETE_CONFIRM;
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
import static com.bipbup.utils.CommandMessageConstants.EDU_SAVE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.MYQUERIES_COMMAND;
import static com.bipbup.utils.CommandMessageConstants.QUERY_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.SCHEDULE_SAVE_PREFIX;
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

    private static final int BUTTONS_PER_ROW = 2;

    private final ConfigService configService;

    private final Encoder encoder;

    public InlineKeyboardMarkup createUserConfigListKeyboard(final AppUser appUser) {
        var configs = configService.getByUser(appUser);
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        configs.forEach(c -> buttons.add(createButtonFromConfig(c)));

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    private InlineKeyboardButton createButtonFromConfig(final AppUserConfig config) {
        var callback = QUERY_PREFIX + encoder.hashOf(config.getUserConfigId());
        return createButton(config.getConfigName(), callback);
    }

    public InlineKeyboardMarkup createConfigManagementKeyboard(final String callbackData) {
        var hash = extractHash(callbackData);

        List<InlineKeyboardButton> buttons = List.of(
                createButton(BUTTON_TEXT_UPDATE, UPDATE_PREFIX + hash),
                createButton(BUTTON_TEXT_DELETE, DELETE_PREFIX + hash),
                createButton(BUTTON_TEXT_BACK, MYQUERIES_COMMAND)
        );

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createDeleteConfirmationKeyboard(final String callbackData) {
        var hash = extractHash(callbackData);

        List<InlineKeyboardButton> buttons = List.of(
                createButton(BUTTON_TEXT_DELETE_CONFIRM, DELETE_CONFIRM_PREFIX + hash),
                createButton(BUTTON_TEXT_DELETE_CANCEL, DELETE_CANCEL_COMMAND)
        );

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createUpdateConfigKeyboard(final String callbackData) {
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

    public InlineKeyboardMarkup createExperienceSelectionKeyboard(final String callbackData) {
        var hash = extractHash(callbackData);

        var buttons = Arrays.stream(ExperienceParam.values())
                .map(p -> createButton(p.getDescription(), p.getPrefix() + hash))
                .toList();

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createEducationLevelSelectionKeyboard(final AppUser user, final String callbackData) {
        var selectedLevels = configService.getSelectedEducationLevels(user.getTelegramId());
        var hash = extractHash(callbackData);

        var buttons = new ArrayList<>(Arrays.stream(EducationLevelParam.values())
                .map(p -> createButtonFromEnum(p, selectedLevels, hash))
                .toList());

        buttons.add(createButton(BUTTON_TEXT_SAVE, EDU_SAVE_PREFIX + hash));

        return createMarkup(buttons, 1);
    }

    public InlineKeyboardMarkup createScheduleTypeSelectionKeyboard(final AppUser user, final String callbackData) {
        var selectedTypes = configService.getSelectedScheduleTypes(user.getTelegramId());
        var hash = extractHash(callbackData);

        var buttons = new ArrayList<>(Arrays.stream(ScheduleTypeParam.values())
                .map(v -> createButtonFromEnum(v, selectedTypes, hash))
                .toList());

        buttons.add(createButton(BUTTON_TEXT_SAVE, SCHEDULE_SAVE_PREFIX + hash));

        return createMarkup(buttons, 1);
    }

    private InlineKeyboardButton createButtonFromEnum(final EnumParam enumParam, final List<? extends EnumParam> params, final String hash) {
        String text = enumParam.getDescription();

        if (params.contains(enumParam))
            text += BUTTON_TEXT_SELECTED;

        String callback = enumParam.getPrefix() + hash;
        return createButton(text, callback);
    }

    private InlineKeyboardButton createButton(final String text, final String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    private InlineKeyboardMarkup createMarkup(final List<InlineKeyboardButton> buttons, final int buttonsPerRow) {
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
