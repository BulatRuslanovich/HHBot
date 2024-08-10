package com.bipbup.controllers;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private final UpdateProcessor updateProcessor;
    @Value("${bot.username}")
    private String botUsername;

    public MyTelegramBot(final UpdateProcessor updateProcessor,
                         final @Value("${bot.token}") String botToken) {
        super(botToken);
        this.updateProcessor = updateProcessor;
    }

    @PostConstruct
    private void init() {
        updateProcessor.registerBot(this);
    }

    @Override
    public void onUpdateReceived(final Update update) {
        updateProcessor.processUpdate(update);
    }

    public void sendAnswerMessage(final SendMessage message) {
        if (message != null) {
            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }
}
