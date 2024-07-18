package com.bipbup.service.impl;

import com.bipbup.service.UpdateProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Log4j
@RequiredArgsConstructor
@Service
public class UpdateProducerImpl implements UpdateProducer {
    private final KafkaTemplate<String, Update> kafkaTemplate;

    @Override
    public void produce(String topic, Update update) {
        kafkaTemplate.send(topic, update);
    }
}
