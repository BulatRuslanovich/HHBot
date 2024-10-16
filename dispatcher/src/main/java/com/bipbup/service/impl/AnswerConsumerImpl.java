package com.bipbup.service.impl;

import com.bipbup.controllers.UpdateProcessor;
import com.bipbup.service.AnswerConsumer;
import com.bipbup.wrapper.impl.EditMessageWrapper;
import com.bipbup.wrapper.impl.SendMessageWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class AnswerConsumerImpl implements AnswerConsumer {

    private final UpdateProcessor updateProcessor;

    @Override
    @KafkaListener(topics = "${topics.answer-topic}", groupId = "groupId")
    public void consumeSendMessage(SendMessageWrapper sendMessage) {
        updateProcessor.sendToTelegram(sendMessage.getMessage());
    }

    @Override
    @KafkaListener(topics = "${topics.edit-topic}", groupId = "groupId")
    public void consumeEditMessage(EditMessageWrapper editMessage) {
        updateProcessor.sendToTelegram(editMessage.getMessage());
    }
}
