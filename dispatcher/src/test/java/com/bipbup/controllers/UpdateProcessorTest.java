package com.bipbup.controllers;

import com.bipbup.service.UpdateProducer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateProcessorTest {

    @Mock
    private UpdateProducer updateProducer;

    @Mock
    private MyTelegramBot myTelegramBot;

    @InjectMocks
    private UpdateProcessor updateProcessor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        updateProcessor.registerBot(myTelegramBot);
    }

    @Test
    @DisplayName("Should return false when update is null")
    void testProcessUpdate_NullUpdate() {
        boolean result = updateProcessor.processUpdate(null);
        assertFalse(result);
    }

    @Test
    @DisplayName("Should process message update correctly")
    void testProcessUpdate_Message() {
        // Arrange
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User user = mock(User.class);

        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.hasText()).thenReturn(true);
        when(message.getText()).thenReturn("Hello");
        when(message.getFrom()).thenReturn(user);
        when(user.getFirstName()).thenReturn("TestUser");

        // Act
        boolean result = updateProcessor.processUpdate(update);

        // Assert
        assertTrue(result);
        verify(updateProducer).produce(any(), eq(update));
    }

    @Test
    @DisplayName("Should process callback query update correctly")
    void testProcessUpdate_CallbackQuery() {
        // Arrange
        Update update = mock(Update.class);
        CallbackQuery callbackQuery = mock(CallbackQuery.class);
        User user = mock(User.class);

        when(update.hasCallbackQuery()).thenReturn(true);
        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getFrom()).thenReturn(user);
        when(user.getFirstName()).thenReturn("TestUser");
        when(callbackQuery.getData()).thenReturn("some_data");

        // Act
        boolean result = updateProcessor.processUpdate(update);

        // Assert
        assertTrue(result);
        verify(updateProducer).produce(any(), eq(update));
    }

    @Test
    @DisplayName("Should deactivate user when kicked")
    void testDeactivateUser() {
        // Arrange
        User user = new User();
        user.setFirstName("TestUser");

        // Act
        updateProcessor.deactivateUser(user);

        // Assert
        verify(updateProducer).produce(any(), any(Update.class));
    }

    @SneakyThrows
    @Test
    @DisplayName("Should send sticker when easter egg is activated")
    void testEasterEgg_SendSticker() {
        // Arrange
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User user = mock(User.class);

        when(update.getMessage()).thenReturn(message);
        when(message.getText()).thenReturn("java");
        when(message.getChatId()).thenReturn(12345L);
        when(message.getFrom()).thenReturn(user);
        when(user.getFirstName()).thenReturn("TestUser");

        // Act
        updateProcessor.easterEgg(update);

        // Assert
        verify(myTelegramBot).execute(any(SendSticker.class));
    }

    @SneakyThrows
    @Test
    @DisplayName("Should not send sticker if no easter egg is activated")
    void testEasterEgg_NoSticker() {
        // Arrange
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User user = mock(User.class);

        when(update.getMessage()).thenReturn(message);
        when(message.getText()).thenReturn("unknown");
        when(message.getChatId()).thenReturn(12345L);
        when(message.getFrom()).thenReturn(user);
        when(user.getFirstName()).thenReturn("TestUser");

        // Act
        updateProcessor.easterEgg(update);

        // Assert
        verify(myTelegramBot, never()).execute(any(SendSticker.class));
    }
}