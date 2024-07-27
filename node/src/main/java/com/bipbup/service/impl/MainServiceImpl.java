package com.bipbup.service.impl;

import com.bipbup.config.KeyboardProperties;
import com.bipbup.enums.AppUserState;
import com.bipbup.handlers.StateHandler;
import com.bipbup.handlers.impl.BasicStateHandler;
import com.bipbup.handlers.impl.ExperienceStateHandler;
import com.bipbup.handlers.impl.QueryStateHandler;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.MainService;
import com.bipbup.utils.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.bipbup.enums.AppUserState.*;

@Slf4j
@Service
public class MainServiceImpl implements MainService {
    private final UserUtil userUtil;
    private final AnswerProducer answerProducer;
    private final KeyboardProperties keyboardProperties;

    private final Map<AppUserState, StateHandler> stateHandlers;

    @Autowired
    public MainServiceImpl(UserUtil userUtil, AnswerProducer answerProducer, KeyboardProperties keyboardProperties, BasicStateHandler basicStateHandler, ExperienceStateHandler experienceStateHandler, QueryStateHandler queryStateHandler) {
        this.userUtil = userUtil;
        this.answerProducer = answerProducer;
        this.keyboardProperties = keyboardProperties;

        this.stateHandlers = Map.of(BASIC_STATE, basicStateHandler,
                WAIT_EXPERIENCE_STATE, experienceStateHandler,
                WAIT_QUERY_STATE, queryStateHandler);
    }

    @Override
    public void processMessage(Update update) {
        var appUser = userUtil.findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        StateHandler handler = stateHandlers.get(userState);
        if (!Objects.isNull(handler)) {
            output = handler.process(appUser, text);
        }

        if (!output.isEmpty()) {
            ReplyKeyboard replyKeyboard = WAIT_EXPERIENCE_STATE.equals(appUser.getState()) ? getExperienceKeyboard() : null;
            sendResponse(output, appUser.getTelegramId(), replyKeyboard);
        }
    }

    private void sendResponse(String output, Long telegramId, ReplyKeyboard replyKeyboard) {
        if (replyKeyboard != null) {
            sendAnswerWithKeyboard(output, telegramId, replyKeyboard);
        } else {
            sendAnswer(output, telegramId);
        }
    }

    private ReplyKeyboard getExperienceKeyboard() {
        var row1 = new KeyboardRow();
        var row2 = new KeyboardRow();
        var row3 = new KeyboardRow();

        row1.add(keyboardProperties.getNoExperience());
        row1.add(keyboardProperties.getOneToThreeYears());
        row2.add(keyboardProperties.getThreeToSixYears());
        row2.add(keyboardProperties.getMoreThanSixYears());
        row3.add(keyboardProperties.getNoFilter());

        return new ReplyKeyboardMarkup(List.of(row1, row2, row3));
    }


    private void sendAnswer(String text, Long chatId) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(new ReplyKeyboardRemove(true))
                .build();

        answerProducer.produceAnswer(sendMessage);
    }

    private void sendAnswerWithKeyboard(String text, Long chatId, ReplyKeyboard replyKeyboard) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(replyKeyboard)
                .build();

        answerProducer.produceAnswer(sendMessage);
    }
}
