package com.bipbup.controllers;

import com.bipbup.config.KafkaTopicProperties;
import com.bipbup.easter.egg.EasterEggService;
import com.bipbup.exception.NullUpdateException;
import com.bipbup.service.UpdateProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateProcessorTest {

    @Mock
    private UpdateProducer updateProducer;

    @Mock
    private EasterEggService easterEggService;

    @Mock
    private KafkaTopicProperties kafkaTopicProperties;

    @Mock
    private HeadHunterBot headHunterBot;

    @InjectMocks
    private UpdateProcessor updateProcessor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        updateProcessor.registerBot(headHunterBot);
    }

    @Test
    @DisplayName("Should return false when update is null")
    void testProcessUpdate_NullUpdate() {
        assertThrows(NullUpdateException.class, () -> updateProcessor.processUpdate(null));
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
        updateProcessor.processUpdate(update);

        // Assert
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
        updateProcessor.processUpdate(update);

        // Assert
        verify(updateProducer).produce(any(), eq(update));
    }
}