package com.bipbup.controllers;

import com.bipbup.service.UpdateProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@RequiredArgsConstructor
@Component
public class UpdateProcessor {

    private final UpdateProducer updateProducer;

    @Value("${spring.kafka.topics.text-update-topic}")
    private String textUpdateTopic;

    @Value("${spring.kafka.topics.callback-query-update-topic}")
    private String callbackQueryUpdateTopic;

    private MyTelegramBot myTelegramBot;

    private static void logEmptyMessageUpdate(final Update update) {
        var status = update.getMyChatMember().getNewChatMember().getStatus();
        var user = update.getMyChatMember().getFrom();
        if (status.equals("kicked")) {
            log.info("User {} block the bot", user.getFirstName());
        } else if (status.equals("member")) {
            log.info("User {} joined", user.getFirstName());
        } else {
            log.error("Message is null");
        }
    }

    public void registerBot(final MyTelegramBot myTelegramBot) {
        this.myTelegramBot = myTelegramBot;
    }

    public boolean processUpdate(final Update update) {
        if (update == null) {
            log.error("Update is null");
            return false;
        }

        if (update.hasMessage()) {
            processMessage(update);
        } else if (update.hasCallbackQuery()) {
            processCallbackQuery(update);
        } else {
            logEmptyMessageUpdate(update);
        }

        return true;
    }

    private void processMessage(Update update) {
        var message = update.getMessage();

        if (message.hasText()) {
            log.info("User {} wrote \"{}\"", message.getFrom().getFirstName(), message.getText());
            updateProducer.produce(textUpdateTopic, update);
        }
    }

    private void processCallbackQuery(final Update update) {
        var callbackQuery = update.getCallbackQuery();

        log.info("User {} sent callback query with data: {}",
                callbackQuery.getFrom().getFirstName(), callbackQuery.getData());
        updateProducer.produce(callbackQueryUpdateTopic, update);
    }

    public void setView(final SendMessage message) {
        try {
            myTelegramBot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error with send message execute", e);
        }
    }

    public void setEdit(final EditMessageText message) {
        try {
            myTelegramBot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error with edit message execute", e);
        }
    }
}
