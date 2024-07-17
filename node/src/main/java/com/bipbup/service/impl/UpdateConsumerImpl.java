package com.bipbup.service.impl;

import com.bipbup.service.AnswerProducer;
import com.bipbup.service.UpdateConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Log4j
@RequiredArgsConstructor
@Service
public class UpdateConsumerImpl implements UpdateConsumer {
    private final AnswerProducer answerProducer;

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.text-update-topic}", groupId = "groupId")
    public void consumeTextUpdate(Update update) {
        log.debug("Update received");

        var message = update.getMessage();
        var sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Hello from Node!");
        answerProducer.produceAnswer(sendMessage);
    }
}
