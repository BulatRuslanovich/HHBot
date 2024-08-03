package com.bipbup.service.impl;

import com.bipbup.controllers.UpdateProcessor;
import com.bipbup.service.AnswerConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;


@RequiredArgsConstructor
@Service
public class AnswerConsumerImpl implements AnswerConsumer {
    private final UpdateProcessor updateProcessor;

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.answer-topic}", groupId = "groupId")
    public void consumeSendMessage(SendMessage sendMessage) {
        updateProcessor.setView(sendMessage);
    }

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.edit-topic}", groupId = "groupId")
    public void consumeEditMessage(EditMessageText editMessage) {
        updateProcessor.setEdit(editMessage);
    }

}
