package com.bipbup.service.impl;

import com.bipbup.service.AnswerProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.interfaces.Validable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

@RequiredArgsConstructor
@Service
public class AnswerProducerImpl implements AnswerProducer {
    @Value("${spring.kafka.topics.answer-topic}")
    private String answerTopic;

    @Value("${spring.kafka.topics.edit-topic}")
    private String editTopic;

    private final KafkaTemplate<String, Validable> kafkaTemplate;

    @Override
    public void produceAnswer(SendMessage sendMessage) {
        kafkaTemplate.send(answerTopic, sendMessage);
    }

    @Override
    public void produceEdit(EditMessageText editMessage) {
        kafkaTemplate.send(editTopic, editMessage);
    }
}
