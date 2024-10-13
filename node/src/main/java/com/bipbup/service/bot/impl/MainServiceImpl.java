package com.bipbup.service.bot.impl;

import com.bipbup.annotation.CallbackQualifier;
import com.bipbup.annotation.MessageQualifier;
import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import static com.bipbup.enums.AppUserState.QUERY_DELETE_STATE;
import static com.bipbup.enums.AppUserState.QUERY_LIST_STATE;
import static com.bipbup.enums.AppUserState.QUERY_MENU_STATE;
import static com.bipbup.enums.AppUserState.QUERY_UPDATE_STATE;
import static com.bipbup.enums.AppUserState.WAIT_EDUCATION_STATE;
import static com.bipbup.enums.AppUserState.WAIT_EXPERIENCE_STATE;
import static com.bipbup.enums.AppUserState.WAIT_SCHEDULE_STATE;
import com.bipbup.handlers.StateHandler;
import com.bipbup.handlers.impl.message.BasicStateHandler;
import com.bipbup.service.bot.MainService;
import com.bipbup.service.cache.UserStateCacheService;
import com.bipbup.service.db.UserService;
import com.bipbup.service.kafka.AnswerProducer;
import static com.bipbup.utils.CommandMessageConstants.Prefix;
import com.bipbup.utils.factory.KeyboardMarkupFactory;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;


@Service
@RequiredArgsConstructor
public class MainServiceImpl implements MainService {

    private final UserService userService;

    private final UserStateCacheService userStateCacheService;

    private final KeyboardMarkupFactory markupFactory;

    private final AnswerProducer answerProducer;

    @Autowired
    @MessageQualifier
    private List<StateHandler> messageStateHandlers;

    @Autowired
    @CallbackQualifier
    private List<StateHandler> callbackStateHandlers;

    private final BasicStateHandler basicStateHandler;


    private final Map<AppUserState, String> statePrefixMap =
            Map.of(QUERY_LIST_STATE, Prefix.QUERY,
                    QUERY_MENU_STATE, Prefix.MENU_STATE,
                    QUERY_DELETE_STATE, Prefix.DELETE_STATE,
                    QUERY_UPDATE_STATE, Prefix.UPDATE_STATE,
                    WAIT_EXPERIENCE_STATE, Prefix.WAIT_EXP_STATE,
                    WAIT_EDUCATION_STATE, Prefix.WAIT_EDU_STATE,
                    WAIT_SCHEDULE_STATE, Prefix.WAIT_SCHEDULE_STATE);


    private static boolean isMultiSelecting(String callbackData) {
        return callbackData.startsWith(Prefix.WAIT_EDU_STATE) || callbackData.startsWith(Prefix.WAIT_SCHEDULE_STATE);
    }

    @Override
    public void processMessage(Update update) {
        var text = update.getMessage().getText();
        var user = userService.findOrSaveAppUser(update);
        var state = userStateCacheService.getUserState(user.getTelegramId());

        var handler = messageStateHandlers.stream()
                .filter(h -> h.state() == state)
                .findFirst().orElse(basicStateHandler);

        var output = handler.process(user, text);

        if (!output.isEmpty()) {
            processMessageOutput(user, output);
        }
    }

    @Override
    public void processCallbackQuery(Update update) {
        var callbackData = update.getCallbackQuery().getData();
        var user = userService.findOrSaveAppUser(update);
        var state = userStateCacheService.getUserState(user.getTelegramId());

        if (callbackData.equals("delete_me_from_db")) {
            userService.deleteAppUser(user);
            return;
        }

        var handler = callbackStateHandlers.stream()
                .filter(c -> c.state() == state)
                .findFirst()
                // если не подходит по state, то ищем по префиксу(как бы, прыгаем по стейтам)
                .orElse(getCallbackStateHandler(callbackData));

        var output = handler.process(user, callbackData);

        if (!output.isEmpty()) {
            processCallbackQueryOutput(user, update.getCallbackQuery(), output);
        }
    }

    private StateHandler getCallbackStateHandler(String callbackData) {
        var prefix = extractPrefix(callbackData);

        return callbackStateHandlers.stream()
                .filter(c -> statePrefixMap.get(c.state()).equals(prefix))
                .findFirst()
                .orElse(basicStateHandler);
    }

    private String extractPrefix(String callbackData) {
        return callbackData.substring(0, callbackData.indexOf('_') + 1);
    }

    private void processMessageOutput(AppUser user, String output) {
        var telegramId = user.getTelegramId();

        sendAnswer(output, telegramId, fetchKeyboardWithoutCallback(user));
    }

    private void processCallbackQueryOutput(AppUser user, CallbackQuery callbackQuery, String output) {
        var callbackData = callbackQuery.getData();
        var telegramId = user.getTelegramId();
        var messageId = callbackQuery.getMessage().getMessageId();
        var state = userStateCacheService.getUserState(telegramId);

        if (!state.isWaiting() || isMultiSelecting(callbackData)) {
            editAnswer(output, telegramId, messageId, fetchKeyboardWithCallback(user, callbackData));
        } else {
            sendAnswer(output, telegramId, fetchKeyboardWithCallback(user, callbackData));
        }
    }

    private void sendAnswer(String text, Long chatId, ReplyKeyboard keyboard) {
        var sendMessage = SendMessage.builder().chatId(chatId).text(text)
                .replyMarkup(keyboard != null ? keyboard : new ReplyKeyboardRemove(true))
                .parseMode(ParseMode.MARKDOWN)
                .build();

        answerProducer.produceAnswer(sendMessage);
    }

    private void editAnswer(String output, Long chatId, Integer messageId, InlineKeyboardMarkup markup) {
        var messageText = EditMessageText.builder()
                .text(output)
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(markup)
                .parseMode("Markdown").build();

        answerProducer.produceEdit(messageText);
    }

    private InlineKeyboardMarkup fetchKeyboardWithCallback(AppUser user, String callbackData) {
        var userState = userStateCacheService.getUserState(user.getTelegramId());

        return switch (userState) {
            case QUERY_MENU_STATE -> markupFactory.createConfigManagementKeyboard(callbackData);
            case QUERY_DELETE_STATE -> markupFactory.createDeleteConfirmationKeyboard(callbackData);
            case QUERY_UPDATE_STATE -> markupFactory.createUpdateConfigKeyboard(callbackData);
            case WAIT_EXPERIENCE_STATE -> markupFactory.createExperienceSelectionKeyboard(callbackData);
            case WAIT_EDUCATION_STATE -> markupFactory.createEducationLevelSelectionKeyboard(user, callbackData);
            case WAIT_SCHEDULE_STATE -> markupFactory.createScheduleTypeSelectionKeyboard(user, callbackData);
            case QUERY_LIST_STATE -> markupFactory.createUserConfigListKeyboard(user);
            default -> null;
        };
    }

    private InlineKeyboardMarkup fetchKeyboardWithoutCallback(AppUser user) {
        var userState = userStateCacheService.getUserState(user.getTelegramId());

        if (userState.equals(QUERY_LIST_STATE)) {
            return markupFactory.createUserConfigListKeyboard(user);
        }

        return null;
    }
}
