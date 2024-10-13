package com.bipbup.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MyTelegramBotTest {
    @Mock
    private UpdateProcessor updateProcessor;

    @InjectMocks
    private MyTelegramBot myTelegramBot;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        myTelegramBot = new MyTelegramBot(updateProcessor, "dummyToken");
    }


    @Test
    void testGetBotPath() {
        // Act
        String path = myTelegramBot.getBotPath();

        // Assert
        assertEquals("/update", path);
    }

    @Test
    void testOnWebhookUpdateReceived() {
        // Act
        BotApiMethod<?> result = myTelegramBot.onWebhookUpdateReceived(null);

        // Assert
        assertNull(result);
    }
}