package com.bipbup.controllers;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class MyTelegramBot extends TelegramLongPollingBot {
    @Value("${bot.username}")
    private String botUsername;
    private final UpdateProcessor updateProcessor;

    @PostConstruct
    private void init() {
        updateProcessor.registerBot(this);
    }

    public MyTelegramBot(UpdateProcessor updateProcessor, @Value("${bot.token}") String botToken) {
        super(botToken);
        this.updateProcessor = updateProcessor;
    }


    @Override
    public void onUpdateReceived(Update update) {
        updateProcessor.processUpdate(update);
    }

    public void sendAnswerMessage(SendMessage message) {
        if (message != null) {
            try {
                SendChatAction chatAction = new SendChatAction();
                chatAction.setChatId(message.getChatId());
                chatAction.setAction(ActionType.TYPING);
                execute(chatAction);

                Thread.sleep(1000);

                execute(message);
            } catch (TelegramApiException | InterruptedException e) {
                log.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }


    @Override
    public String getBotUsername() {
        return botUsername;
    }
}
