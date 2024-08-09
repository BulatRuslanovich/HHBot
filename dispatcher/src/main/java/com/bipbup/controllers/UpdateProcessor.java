package com.bipbup.controllers;

import com.bipbup.service.UpdateProducer;
import com.bipbup.utils.MessageUtil;
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
    @Value("${spring.kafka.topics.text-update-topic}")
    private String textUpdateTopic;
    @Value("${spring.kafka.topics.callback-query-update-topic}")
    private String callbackQueryUpdateTopic;
    private MyTelegramBot myTelegramBot;
    private final MessageUtil messageUtil;
    private final UpdateProducer updateProducer;

    public void registerBot(final MyTelegramBot myTelegramBot) {
        this.myTelegramBot = myTelegramBot;
    }

    public void processUpdate(final Update update) {
        if (update == null) {
            log.error("Update is null");
            return;
        }

        if (update.hasMessage()) {
            processMessage(update);
        } else if (update.hasCallbackQuery()) {
            processCallbackQuery(update);
        } else {
            logEmptyMessageUpdate(update);
        }
    }

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

    private void processMessage(Update update) {
        var message = update.getMessage();

        if (message.hasText()) {
            log.info("{} write \"{}\"",
                    message.getFrom().getFirstName(),
                    message.getText());

            updateProducer.produce(textUpdateTopic, update);
        } else {
            setUnsupportedMessageType(update);
        }
    }

    private void processCallbackQuery(final Update update) {
        var callbackQuery = update.getCallbackQuery();

        if (callbackQuery != null) {
            log.debug("{} sent callback query with data: {}",
                    callbackQuery.getFrom().getFirstName(),
                    callbackQuery.getData());

            updateProducer.produce(callbackQueryUpdateTopic, update);
        } else {
            log.error("Update has no callback query");
        }
    }

    private void setUnsupportedMessageType(final Update update) {
        var sendMessage = messageUtil.generateSendMessage(update,
                "Неподдерживаемый тип данных!");
        setView(sendMessage);
    }

    public void setView(final SendMessage sendMessage) {
        myTelegramBot.sendAnswerMessage(sendMessage);
    }

    public void setEdit(final EditMessageText editMessage) {
        try {
            myTelegramBot.execute(editMessage);
        } catch (TelegramApiException e) {
            log.error("Error with edit message execute");
        }
    }
}
