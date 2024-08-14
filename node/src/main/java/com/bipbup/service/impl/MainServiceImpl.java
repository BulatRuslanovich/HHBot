package com.bipbup.service.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import com.bipbup.handlers.StateHandler;
import com.bipbup.handlers.impl.BasicStateHandler;
import com.bipbup.handlers.impl.QueryDeleteStateHandler;
import com.bipbup.handlers.impl.QueryListStateHandler;
import com.bipbup.handlers.impl.QueryMenuStateHandler;
import com.bipbup.handlers.impl.QueryUpdateStateHandler;
import com.bipbup.handlers.impl.WaitAreaStateHandler;
import com.bipbup.handlers.impl.WaitConfigNameStateHandler;
import com.bipbup.handlers.impl.WaitExperienceStateHandler;
import com.bipbup.handlers.impl.WaitQueryStateHandler;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.MainService;
import com.bipbup.service.UserService;
import com.bipbup.utils.factory.KeyboardMarkupFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.Map;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.QUERY_DELETE_STATE;
import static com.bipbup.enums.AppUserState.QUERY_LIST_STATE;
import static com.bipbup.enums.AppUserState.QUERY_MENU_STATE;
import static com.bipbup.enums.AppUserState.QUERY_UPDATE_STATE;
import static com.bipbup.enums.AppUserState.WAIT_AREA_STATE;
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import static com.bipbup.enums.AppUserState.WAIT_EXPERIENCE_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;
import static com.bipbup.utils.CommandMessageConstants.DELETE_STATE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.MENU_STATE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.MYQUERIES_COMMAND;
import static com.bipbup.utils.CommandMessageConstants.QUERY_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.UPDATE_STATE_PREFIX;
import static com.bipbup.utils.CommandMessageConstants.WAIT_EXP_STATE_PREFIX;


@Service
public class MainServiceImpl implements MainService {
    private final UserService userService;

    private final KeyboardMarkupFactory markupFactory;

    private final AnswerProducer answerProducer;

    private final Map<AppUserState, StateHandler> messageStateHandlers;

    private final Map<AppUserState, StateHandler> callbackStateHandlers;

    public MainServiceImpl(final UserService userService,
                           final KeyboardMarkupFactory markupFactory,
                           final AnswerProducer answerProducer,
                           final BasicStateHandler basicStateHandler,
                           final WaitConfigNameStateHandler waitConfigNameStateHandle,
                           final WaitQueryStateHandler waitQueryStateHandler,
                           final QueryListStateHandler queryListStateHandler,
                           final QueryMenuStateHandler queryMenuStateHandler,
                           final QueryDeleteStateHandler queryDeleteStateHandler,
                           final QueryUpdateStateHandler queryUpdateStateHandler,
                           final WaitExperienceStateHandler waitExperienceStateHandler,
                           final WaitAreaStateHandler waitAreaStateHandler) {
        this.userService = userService;
        this.markupFactory = markupFactory;
        this.answerProducer = answerProducer;

        this.messageStateHandlers = Map.of(BASIC_STATE, basicStateHandler,
                WAIT_CONFIG_NAME_STATE, waitConfigNameStateHandle,
                WAIT_QUERY_STATE, waitQueryStateHandler,
                WAIT_AREA_STATE, waitAreaStateHandler);

        this.callbackStateHandlers = Map.of(QUERY_LIST_STATE, queryListStateHandler,
                QUERY_MENU_STATE, queryMenuStateHandler,
                QUERY_DELETE_STATE, queryDeleteStateHandler,
                QUERY_UPDATE_STATE, queryUpdateStateHandler,
                WAIT_EXPERIENCE_STATE, waitExperienceStateHandler);
    }

    @Override
    public void processMessage(final Update update) {
        var text = update.getMessage().getText();
        var user = userService.findOrSaveAppUser(update);
        var userState = userService.getUserState(user.getTelegramId());
        var handler = messageStateHandlers.getOrDefault(userState, messageStateHandlers.get(BASIC_STATE));
        var output = handler.process(user, text);

        if (!output.isEmpty())
            processOutput(user, update, output);
    }

    @Override
    public void processCallbackQuery(final Update update) {
        var callbackData = update.getCallbackQuery().getData();
        var user = userService.findOrSaveAppUser(update);
        var userState = userService.getUserState(user.getTelegramId());

        var handler = callbackStateHandlers.getOrDefault(userState, getCallbackStateHandler(callbackData));
        var output = handler.process(user, callbackData);

        if (!output.isEmpty())
            processOutput(user, update, output);
    }

    private StateHandler getCallbackStateHandler(String callbackData) {
        if (callbackData.startsWith(QUERY_PREFIX))
            return callbackStateHandlers.get(QUERY_LIST_STATE);
        if (callbackData.startsWith(MENU_STATE_PREFIX) || callbackData.equals(MYQUERIES_COMMAND))
            return callbackStateHandlers.get(QUERY_MENU_STATE);
        if (callbackData.startsWith(UPDATE_STATE_PREFIX))
            return callbackStateHandlers.get(QUERY_UPDATE_STATE);
        if (callbackData.startsWith(DELETE_STATE_PREFIX))
            return callbackStateHandlers.get(QUERY_DELETE_STATE);
        if (callbackData.startsWith(WAIT_EXP_STATE_PREFIX))
            return callbackStateHandlers.get(WAIT_EXPERIENCE_STATE);

        return messageStateHandlers.get(BASIC_STATE);
    }

    private void processOutput(final AppUser user,
                               final Update update,
                               final String output) {
        var userTelegramId = user.getTelegramId();
        var userState = userService.getUserState(userTelegramId);
        if (userState.equals(QUERY_LIST_STATE) && !update.hasCallbackQuery())
            sendAnswer(output, userTelegramId, markupFactory.createUserConfigListKeyboard(user));
        else if (update.hasCallbackQuery())
            processCallbackQueryOutput(user, update.getCallbackQuery(), output);
        else
            sendAnswer(output, userTelegramId);
    }

    private void processCallbackQueryOutput(final AppUser user,
                                            final CallbackQuery callbackQuery,
                                            final String output) {
        var callbackData = callbackQuery.getData();
        var userTelegramId = user.getTelegramId();

        var userState = userService.getUserState(userTelegramId);
        var isWaitState = userState.toString().startsWith("WAIT_");
        if (isWaitState) {
            var isUpdating = callbackData.startsWith(UPDATE_STATE_PREFIX);
            if (isUpdating)
                sendAnswer(output, userTelegramId, fetchKeyboard(user, callbackData));
            else
                sendAnswer(output, userTelegramId);
        } else
            editAnswer(output, userTelegramId, callbackQuery.getMessage().getMessageId(),
                    fetchKeyboard(user, callbackData));
    }

    private void sendAnswer(final String text,
                            final Long chatId) {
        sendAnswer(text, chatId, null);
    }

    private void sendAnswer(final String text,
                            final Long chatId,
                            final ReplyKeyboard keyboard) {
        var sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard != null ? keyboard : new ReplyKeyboardRemove(true))
                .build();

        answerProducer.produceAnswer(sendMessage);
    }

    private void editAnswer(final String output,
                            final Long chatId,
                            final Integer messageId,
                            final InlineKeyboardMarkup markup) {
        var messageText = EditMessageText.builder()
                .text(output)
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(markup)
                .build();

        answerProducer.produceEdit(messageText);
    }

    private InlineKeyboardMarkup fetchKeyboard(final AppUser user,
                                               final String callbackData) {
        var userState = userService.getUserState(user.getTelegramId());
        if (userState.equals(QUERY_LIST_STATE))
            return markupFactory.createUserConfigListKeyboard(user);
        if (userState.equals(QUERY_MENU_STATE))
            return markupFactory.createConfigManagementKeyboard(callbackData);
        if (userState.equals(QUERY_DELETE_STATE))
            return markupFactory.createDeleteConfirmationKeyboard(callbackData);
        if (userState.equals(QUERY_UPDATE_STATE))
            return markupFactory.createUpdateConfigKeyboard(callbackData);
        if (userState.equals(WAIT_EXPERIENCE_STATE))
            return markupFactory.createExperienceSelectionKeyboard(callbackData);

        return null;
    }
}

