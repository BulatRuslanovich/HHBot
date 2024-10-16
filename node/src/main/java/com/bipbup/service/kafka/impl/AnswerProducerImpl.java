package com.bipbup.service.kafka.impl;

import com.bipbup.config.KafkaTopicProperties;
import com.bipbup.service.kafka.AnswerProducer;
import com.bipbup.wrapper.MessageWrapper;
import com.bipbup.wrapper.impl.EditMessageWrapper;
import com.bipbup.wrapper.impl.SendMessageWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

@RequiredArgsConstructor
@Service
public class AnswerProducerImpl implements AnswerProducer {

    private final KafkaTopicProperties kafkaTopicProperties;

    private final KafkaTemplate<String, MessageWrapper> kafkaTemplate;

    @Override
    public void produceAnswer(SendMessage sendMessage) {
        kafkaTemplate.send(kafkaTopicProperties.answerTopic(), SendMessageWrapper.of(sendMessage));
    }

    @Override
    public void produceEdit(EditMessageText editMessage) {
        kafkaTemplate.send(kafkaTopicProperties.editTopic(), EditMessageWrapper.of(editMessage));
    }
}
