package com.bipbup.controllers;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class MyTelegramBot extends TelegramWebhookBot {

    @Value("${bot.uri}")
    private String botUri;
    @Value("${bot.username}")
    private String botUsername;

    private final UpdateProcessor updateProcessor;

    public MyTelegramBot(final UpdateProcessor updateProcessor,
                         final @Value("${bot.token}") String botToken) {
        super(botToken);
        this.updateProcessor = updateProcessor;
    }

    @PostConstruct
    private void init() {
        updateProcessor.registerBot(this);

        try {
            var webhook = SetWebhook.builder()
                    .url(botUri)
                    .build();
            this.setWebhook(webhook);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
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

    @Override
    public String getBotPath() {
        return "/update";
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return null;
    }
}
