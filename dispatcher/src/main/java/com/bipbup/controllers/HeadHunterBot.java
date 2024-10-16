package com.bipbup.controllers;

import com.bipbup.config.TelegramBotProperties;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class HeadHunterBot extends TelegramWebhookBot {

    private final UpdateProcessor updateProcessor;

    private final TelegramBotProperties telegramBotProperties;

    public HeadHunterBot(UpdateProcessor updateProcessor, TelegramBotProperties telegramBotProperties) {
        super(telegramBotProperties.getToken());
        this.updateProcessor = updateProcessor;
        this.telegramBotProperties = telegramBotProperties;
    }

    @PostConstruct
    private void init() {
        updateProcessor.registerBot(this);

        try {
            var webhook = SetWebhook.builder()
                    .url(telegramBotProperties.getUrl())
                    .build();
            this.setWebhook(webhook);
        } catch (TelegramApiException e) {
            log.error("Error with webhook", e);
        }
    }

    @Override
    public String getBotUsername() {
        return telegramBotProperties.getUsername();
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
