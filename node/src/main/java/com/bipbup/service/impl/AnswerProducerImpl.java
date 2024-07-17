package com.bipbup.service.impl;

import com.bipbup.service.AnswerProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@RequiredArgsConstructor
@Service
public class AnswerProducerImpl implements AnswerProducer {
    @Value("${spring.kafka.topics.answer-topic}")
    private String answerTopic;

    private final KafkaTemplate<String, SendMessage> kafkaTemplate;

    @Override
    public void produceAnswer(SendMessage sendMessage) {
        kafkaTemplate.send(answerTopic, sendMessage);
    }
}
