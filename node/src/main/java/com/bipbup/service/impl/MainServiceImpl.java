package com.bipbup.service.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.AppUserState;
import com.bipbup.handlers.StateHandler;
import com.bipbup.handlers.impl.BasicStateHandler;
import com.bipbup.handlers.impl.WaitConfigNameStateHandle;
import com.bipbup.handlers.impl.WaitQueryStateHandler;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.MainService;
import com.bipbup.utils.UserUtil;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
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
    private final AnswerProducer answerProducer;
    private final Map<AppUserState, StateHandler> stateHandlers;


    public MainServiceImpl(UserUtil userUtil, AnswerProducer answerProducer, BasicStateHandler basicStateHandler, WaitConfigNameStateHandle waitConfigNameStateHandle, WaitQueryStateHandler waitQueryStateHandler) {
        this.userUtil = userUtil;
        this.answerProducer = answerProducer;

        this.stateHandlers = Map.of(BASIC_STATE, basicStateHandler,
                WAIT_CONFIG_NAME_STATE, waitConfigNameStateHandle,
                WAIT_QUERY_STATE, waitQueryStateHandler);
    }

    @Override
    public void processMessage(Update update) {
        var text = update.getMessage().getText();
        processUpdate(update, text);
    }

    @Override
    public void processCallbackQuery(Update update) {
        var callbackData = update.getCallbackQuery().getData();
        processUpdate(update, callbackData);
    }

    private void processUpdate(Update update, String data) {
        var appUser = userUtil.findOrSaveAppUser(update);
        var userState = appUser.getState();
        var output = "";

        StateHandler handler = stateHandlers.get(userState);
        if (handler != null) {
            output = handler.process(appUser, data);
        }

        //TODO: понимаю что так себе код, это так просто проверить
        if (!output.isEmpty()) {
            ReplyKeyboard replyKeyboard;
            if (update.hasCallbackQuery()) {
                if (update.getCallbackQuery().getData().startsWith("query_")) {
                    replyKeyboard = getBackToQueryList();
                } else {
                    replyKeyboard = getQueryListKeyboard(appUser);
                }
                editAnswer(output, appUser.getTelegramId(), update.getCallbackQuery().getMessage().getMessageId(), (InlineKeyboardMarkup) replyKeyboard);
            } else {
                sendAnswer(output, appUser.getTelegramId());
            }
        }
    }

    private void sendAnswer(String text, Long chatId) {
        sendAnswer(text, chatId, null);
    }

    private void sendAnswer(String text, Long chatId, ReplyKeyboard keyboard) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard != null ? keyboard : new ReplyKeyboardRemove(true))
                .build();

        answerProducer.produceAnswer(sendMessage);
    }

    private void editAnswer(String output, Long telegramId, Integer messageId, InlineKeyboardMarkup markup) {
        EditMessageText messageText = EditMessageText.builder()
                .text(output)
                .chatId(telegramId)
                .messageId(messageId)
                .replyMarkup(markup)
                .build();

        answerProducer.produceEdit(messageText);
    }


    private ReplyKeyboard getQueryListKeyboard(AppUser appUser) {
        List<AppUserConfig> appUserConfigs = appUser.getAppUserConfigs();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (var appUserConfig : appUserConfigs) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(appUserConfig.getConfigName())
                    .callbackData(String.format("query_%s", appUserConfig.getUserConfigId()))
                    .build());
            rows.add(row);
        }

        var inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rows);

        return inlineKeyboardMarkup;
    }

    private InlineKeyboardMarkup getBackToQueryList() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = InlineKeyboardButton.builder()
                .text("Назад")
                .callbackData("back_to_query_list")
                .build();

        rows.add(List.of(inlineKeyboardButton));

        var inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(rows);

        return inlineKeyboardMarkup;
    }
}

