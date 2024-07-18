package com.bipbup.controllers;

import com.bipbup.service.UpdateProducer;
import com.bipbup.utils.MessageUtil;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Log4j
@Component
public class UpdateProcessor {
    @Value("${spring.kafka.topics.text-update-topic}")
    private String textUpdateTopic;
    private MyTelegramBot myTelegramBot;
    private final MessageUtil messageUtil;
    private final UpdateProducer updateProducer;

    public UpdateProcessor(MessageUtil messageUtil, UpdateProducer updateProducer) {
        this.messageUtil = messageUtil;
        this.updateProducer = updateProducer;
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
            updateProducer.produce(textUpdateTopic, update);
        } else {
            setUnsupportedMessageType(update);
        }
    }

    private void setUnsupportedMessageType(Update update) {
        var sendMessage = messageUtil.generateSendMessage(update, "Неподдерживаемый тип данных!");
        setView(sendMessage);
    }

    public void setView(SendMessage sendMessage) {
        myTelegramBot.sendAnswerMessage(sendMessage);
    }
}
