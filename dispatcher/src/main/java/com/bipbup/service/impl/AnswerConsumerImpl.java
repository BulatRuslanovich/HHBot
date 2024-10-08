package com.bipbup.service.impl;

import com.bipbup.controllers.UpdateProcessor;
import com.bipbup.service.AnswerConsumer;
import com.bipbup.wrapper.impl.EditMessageWrapper;
import com.bipbup.wrapper.impl.SendMessageWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@RequiredArgsConstructor
@Service
public class AnswerConsumerImpl implements AnswerConsumer {

    private final UpdateProcessor updateProcessor;

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.answer-topic}", groupId = "groupId")
    public void consumeSendMessage(final SendMessageWrapper sendMessage) {
        updateProcessor.setView(sendMessage.getMessage());
    }

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.edit-topic}", groupId = "groupId")
    public void consumeEditMessage(final EditMessageWrapper editMessage) {
        updateProcessor.setEdit(editMessage.getMessage());
    }
}
