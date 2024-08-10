package com.bipbup.service.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.AppUserState;
import com.bipbup.handlers.StateHandler;
import com.bipbup.handlers.impl.BasicStateHandler;
import com.bipbup.handlers.impl.QueryDeleteStateHandler;
import com.bipbup.handlers.impl.QueryListStateHandler;
import com.bipbup.handlers.impl.QueryMenuStateHandler;
import com.bipbup.handlers.impl.QueryUpdateStateHandler;
import com.bipbup.handlers.impl.WaitConfigNameStateHandler;
import com.bipbup.handlers.impl.WaitQueryStateHandler;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.MainService;
import com.bipbup.utils.UserUtil;
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
import static com.bipbup.enums.AppUserState.WAIT_CONFIG_NAME_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;


@Service
public class MainServiceImpl implements MainService {
    private final UserUtil userUtil;
    private final KeyboardMarkupFactory markupFactory;
    private final AnswerProducer answerProducer;
    private final Map<AppUserState, StateHandler> stateHandlers;

    public MainServiceImpl(final UserUtil userUtil,
                           final KeyboardMarkupFactory markupFactory,
                           final AnswerProducer answerProducer,
                           final BasicStateHandler basicStateHandler,
                           final WaitConfigNameStateHandler waitConfigNameStateHandle,
                           final WaitQueryStateHandler waitQueryStateHandler,
                           final QueryListStateHandler queryListStateHandler,
                           final QueryMenuStateHandler queryMenuStateHandler,
                           final QueryDeleteStateHandler queryDeleteStateHandler,
                           final QueryUpdateStateHandler queryUpdateStateHandler) {
        this.userUtil = userUtil;
        this.markupFactory = markupFactory;
        this.answerProducer = answerProducer;

        this.stateHandlers = Map.of(BASIC_STATE, basicStateHandler,
                WAIT_CONFIG_NAME_STATE, waitConfigNameStateHandle,
                WAIT_QUERY_STATE, waitQueryStateHandler,
                QUERY_LIST_STATE, queryListStateHandler,
                QUERY_MENU_STATE, queryMenuStateHandler,
                QUERY_DELETE_STATE, queryDeleteStateHandler,
                QUERY_UPDATE_STATE, queryUpdateStateHandler);
    }

    @Override
    public void processMessage(final Update update) {
        var text = update.getMessage().getText();
        processUpdate(update, text);
    }

    @Override
    public void processCallbackQuery(final Update update) {
        var callbackData = update.getCallbackQuery().getData();
        processUpdate(update, callbackData);
    }

    private void processUpdate(final Update update,
                               final String data) {
        var appUser = userUtil.findOrSaveAppUser(update);
        var userState = appUser.getState();
        var output = "";

        var handler = stateHandlers.get(userState);
        if (handler != null) {
            output = handler.process(appUser, data);
        }

        if (!output.isEmpty()) {
            if (appUser.getState().equals(QUERY_LIST_STATE) && !update.hasCallbackQuery()) {
                sendAnswer(output, appUser.getTelegramId(), markupFactory.createUserConfigListKeyboard(appUser));
            } else if (update.hasCallbackQuery()) {
                var callbackQuery = update.getCallbackQuery();
                editAnswer(output, appUser.getTelegramId(), callbackQuery.getMessage().getMessageId(),
                        getKeyboard(appUser, callbackQuery));
            } else {
                sendAnswer(output, appUser.getTelegramId());
            }
        }
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

    private InlineKeyboardMarkup getKeyboard(final AppUser appUser,
                                             final CallbackQuery callbackQuery) {
        var userState = appUser.getState();
        if (userState.equals(QUERY_LIST_STATE)) {
            return markupFactory.createUserConfigListKeyboard(appUser);
        } else if (userState.equals(QUERY_MENU_STATE)) {
            return markupFactory.createConfigManagementKeyboard(callbackQuery);
        } else if (userState.equals(QUERY_DELETE_STATE)) {
            return markupFactory.createDeleteConfirmationKeyboard(callbackQuery);
        } else if (userState.equals(QUERY_UPDATE_STATE)) {
            return markupFactory.createUpdateConfigKeyboard(callbackQuery);
        } else {
            return null;
        }
    }
}

