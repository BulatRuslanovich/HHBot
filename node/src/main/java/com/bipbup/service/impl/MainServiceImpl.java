package com.bipbup.service.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.AppUserState;
import com.bipbup.handlers.StateHandler;
import com.bipbup.handlers.impl.*;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.MainService;
import com.bipbup.utils.UserUtil;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.bipbup.enums.AppUserState.*;


@Service
public class MainServiceImpl implements MainService {
    private final UserUtil userUtil;
    private final AppUserConfigDAO appUserConfigDAO;
    private final AnswerProducer answerProducer;
    private final Map<AppUserState, StateHandler> stateHandlers;

    public MainServiceImpl(final UserUtil userUtil,
                           final AppUserConfigDAO appUserConfigDAO,
                           final AnswerProducer answerProducer,
                           final BasicStateHandler basicStateHandler,
                           final WaitConfigNameStateHandler waitConfigNameStateHandle,
                           final WaitQueryStateHandler waitQueryStateHandler,
                           final QueryListStateHandler queryListStateHandler,
                           final QueryMenuStateHandler queryMenuStateHandler,
                           final QueryDeleteStateHandler queryDeleteStateHandler) {
        this.userUtil = userUtil;
        this.appUserConfigDAO = appUserConfigDAO;
        this.answerProducer = answerProducer;

        this.stateHandlers = Map.of(BASIC_STATE, basicStateHandler,
                WAIT_CONFIG_NAME_STATE, waitConfigNameStateHandle,
                WAIT_QUERY_STATE, waitQueryStateHandler,
                QUERY_LIST_STATE, queryListStateHandler,
                QUERY_MENU_STATE, queryMenuStateHandler,
                QUERY_DELETE_STATE, queryDeleteStateHandler);
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

        StateHandler handler = stateHandlers.get(userState);
        if (handler != null) {
            output = handler.process(appUser, data);
        }

        if (!output.isEmpty()) {
            if (appUser.getState().equals(QUERY_LIST_STATE) && !update.hasCallbackQuery()) {
                sendAnswer(output, appUser.getTelegramId(), getQueryListKeyboard(appUser));
            } else if (appUser.getState().equals(QUERY_LIST_STATE) && update.hasCallbackQuery()) {
                editAnswer(output, appUser.getTelegramId(), update.getCallbackQuery().getMessage().getMessageId(), getQueryListKeyboard(appUser));
            } else if (appUser.getState().equals(QUERY_MENU_STATE) && update.hasCallbackQuery()) {
                editAnswer(output, appUser.getTelegramId(), update.getCallbackQuery().getMessage().getMessageId(), getQueryMenuKeyboard(update.getCallbackQuery()));
            } else if (appUser.getState().equals(QUERY_DELETE_STATE) && update.hasCallbackQuery()) {
                editAnswer(output, appUser.getTelegramId(), update.getCallbackQuery().getMessage().getMessageId(), getQueryDeleteKeyboard(update.getCallbackQuery()));
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
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard != null
                        ? keyboard
                        : new ReplyKeyboardRemove(true))
                .build();

        answerProducer.produceAnswer(sendMessage);
    }

    private void editAnswer(final String output,
                            final Long telegramId,
                            final Integer messageId,
                            final InlineKeyboardMarkup markup) {
        EditMessageText messageText = EditMessageText.builder()
                .text(output)
                .chatId(telegramId)
                .messageId(messageId)
                .replyMarkup(markup)
                .build();

        answerProducer.produceEdit(messageText);
    }


    private InlineKeyboardMarkup getQueryListKeyboard(final AppUser appUser) {
        List<AppUserConfig> appUserConfigs = appUserConfigDAO.findByAppUser(appUser);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> currentRow = new ArrayList<>();
        for (var appUserConfig : appUserConfigs) {
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(appUserConfig.getConfigName())
                    .callbackData(String.format("query_%s", appUserConfig.getUserConfigId()))
                    .build();

            currentRow.add(button);

            if (currentRow.size() == 2) {
                rows.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }

        if (!currentRow.isEmpty()) {
            rows.add(currentRow);
        }

        var inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rows);

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getQueryMenuKeyboard(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        String configId = data.substring("query_".length());

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Create buttons
        InlineKeyboardButton refreshButton = InlineKeyboardButton.builder()
                .text("Обновить")
                .callbackData("update_" + configId)
                .build();

        InlineKeyboardButton deleteButton = InlineKeyboardButton.builder()
                .text("Удалить")
                .callbackData("delete_" + configId)
                .build();

        InlineKeyboardButton backButton = InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData("back_to_query_list")
                .build();

        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        firstRow.add(refreshButton);
        firstRow.add(deleteButton);

        List<InlineKeyboardButton> secondRow = new ArrayList<>();
        secondRow.add(backButton);

        rows.add(firstRow);
        rows.add(secondRow);

        var inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rows);

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getQueryDeleteKeyboard(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        String configId = data.substring("delete_".length());

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton deleteButton = InlineKeyboardButton.builder()
                .text("Да, удалить")
                .callbackData("delete_yes_" + configId)
                .build();

        InlineKeyboardButton cancelButton = InlineKeyboardButton.builder()
                .text("Нет, не удалять")
                .callbackData("delete_no")
                .build();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(deleteButton);
        row.add(cancelButton);

        rows.add(row);
        var inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rows);

        return inlineKeyboardMarkup;
    }
}

