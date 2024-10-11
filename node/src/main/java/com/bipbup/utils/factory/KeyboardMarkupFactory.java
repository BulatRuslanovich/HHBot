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

import static com.bipbup.utils.CommandMessageConstants.BotCommand.MYQUERIES;
import static com.bipbup.utils.CommandMessageConstants.ButtonText;
import static com.bipbup.utils.CommandMessageConstants.Prefix;

@RequiredArgsConstructor
@Component
public class KeyboardMarkupFactory {

    private static final int BUTTONS_PER_ROW = 2;

    private final ConfigService configService;

    private final Encoder encoder;

    public InlineKeyboardMarkup createUserConfigListKeyboard(AppUser appUser) {
        var configs = configService.getByUser(appUser);
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        configs.forEach(c -> buttons.add(createButtonFromConfig(c)));

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    private InlineKeyboardButton createButtonFromConfig(AppUserConfig config) {
        var callback = Prefix.QUERY + encoder.hashOf(config.getId());
        return createButton(config.getConfigName(), callback);
    }

    public InlineKeyboardMarkup createConfigManagementKeyboard(String callbackData) {
        var hash = extractHash(callbackData);

        List<InlineKeyboardButton> buttons = List.of(
                createButton(ButtonText.UPDATE, Prefix.UPDATE + hash),
                createButton(ButtonText.DELETE, Prefix.DELETE + hash),
                createButton(ButtonText.BACK, MYQUERIES.getCommand())
        );

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createDeleteConfirmationKeyboard(String callbackData) {
        var hash = extractHash(callbackData);

        List<InlineKeyboardButton> buttons = List.of(
                createButton(ButtonText.DELETE_CONFIRM, Prefix.DELETE_CONFIRM + hash),
                createButton(ButtonText.DELETE_CANCEL, Prefix.QUERY + hash)
        );

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createUpdateConfigKeyboard(String callbackData) {
        var hash = extractHash(callbackData);

        List<InlineKeyboardButton> buttons = List.of(
                createButton(ButtonText.UPDATE_CONFIG_NAME, Prefix.UPDATE_CONFIG_NAME + hash),
                createButton(ButtonText.UPDATE_QUERY, Prefix.UPDATE_QUERY + hash),
                createButton(ButtonText.UPDATE_AREA, Prefix.UPDATE_AREA + hash),
                createButton(ButtonText.UPDATE_EXPERIENCE, Prefix.UPDATE_EXPERIENCE + hash),
                createButton(ButtonText.UPDATE_EDUCATION, Prefix.UPDATE_EDUCATION + hash),
                createButton(ButtonText.UPDATE_SCHEDULE, Prefix.UPDATE_SCHEDULE + hash),
                createButton(ButtonText.BACK, Prefix.QUERY + hash)
        );

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createExperienceSelectionKeyboard(String callbackData) {
        var hash = extractHash(callbackData);

        var buttons = Arrays.stream(ExperienceParam.values())
                .map(p -> createButton(p.getDescription(), p.getPrefix() + hash))
                .toList();

        return createMarkup(buttons, BUTTONS_PER_ROW);
    }

    public InlineKeyboardMarkup createEducationLevelSelectionKeyboard(AppUser user, String callbackData) {
        var selectedLevels = configService.getSelectedEducationLevels(user.getTelegramId());
        var hash = extractHash(callbackData);

        var buttons = new ArrayList<>(Arrays.stream(EducationLevelParam.values())
                .map(p -> createButtonFromEnum(p, selectedLevels, hash))
                .toList());

        buttons.add(createButton(ButtonText.SAVE, Prefix.EDU_SAVE + hash));

        return createMarkup(buttons, 1);
    }

    public InlineKeyboardMarkup createScheduleTypeSelectionKeyboard(AppUser user, String callbackData) {
        var selectedTypes = configService.getSelectedScheduleTypes(user.getTelegramId());
        var hash = extractHash(callbackData);

        var buttons = new ArrayList<>(Arrays.stream(ScheduleTypeParam.values())
                .map(v -> createButtonFromEnum(v, selectedTypes, hash))
                .toList());

        buttons.add(createButton(ButtonText.SAVE, Prefix.SCHEDULE_SAVE + hash));

        return createMarkup(buttons, 1);
    }

    private InlineKeyboardButton createButtonFromEnum(EnumParam enumParam, List<? extends EnumParam> params, String hash) {
        String text = enumParam.getDescription();

        if (params.contains(enumParam))
            text += " " + ButtonText.SELECTED;

        String callback = enumParam.getPrefix() + hash;
        return createButton(text, callback);
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
