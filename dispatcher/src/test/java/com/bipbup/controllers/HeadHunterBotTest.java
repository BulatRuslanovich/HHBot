package com.bipbup.controllers;

import com.bipbup.config.TelegramBotProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HeadHunterBotTest {
    @Mock
    private UpdateProcessor updateProcessor;

    @Mock
    private TelegramBotProperties telegramBotProperties;

    @InjectMocks
    private HeadHunterBot headHunterBot;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        headHunterBot = new HeadHunterBot(updateProcessor, telegramBotProperties);
    }


    @Test
    void testGetBotPath() {
        // Act
        String path = headHunterBot.getBotPath();

        // Assert
        assertEquals("/update", path);
    }

    @Test
    void testOnWebhookUpdateReceived() {
        // Act
        BotApiMethod<?> result = headHunterBot.onWebhookUpdateReceived(null);

        // Assert
        assertNull(result);
    }
}