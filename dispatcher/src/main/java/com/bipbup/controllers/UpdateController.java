package com.bipbup.controllers;

import com.bipbup.utils.MessageUtil;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Log4j
@Component
public class UpdateController {
    private MyTelegramBot myTelegramBot;
    private final MessageUtil messageUtil;

    public UpdateController(MessageUtil messageUtil) {
        this.messageUtil = messageUtil;
    }

    public void registerBot(MyTelegramBot myTelegramBot) {
        this.myTelegramBot = myTelegramBot;
    }

    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Update is null");
            return;
        }

        if (update.hasMessage()) {
            processMessage(update);
        } else {
            log.error("Message is null");
        }
    }

    private void processMessage(Update update) {
        var message = update.getMessage();

        if (message.hasText()) {
            log.info(message.getText());
        } else {
            setUnsupportedMessageType(update);
        }
    }

    private void setUnsupportedMessageType(Update update) {
        var sendMessage = messageUtil.generateSendMessage(update, "Неподдерживаемый тип данных!");
        setView(sendMessage);
    }

    private void setView(SendMessage sendMessage) {
        myTelegramBot.sendAnswerMessage(sendMessage);
    }
}
