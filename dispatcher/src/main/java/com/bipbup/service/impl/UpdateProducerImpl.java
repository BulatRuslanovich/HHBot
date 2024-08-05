package com.bipbup.service.impl;

import com.bipbup.service.UpdateProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Service
public class UpdateProducerImpl implements UpdateProducer {
    private final KafkaTemplate<String, Update> kafkaTemplate;

    @Override
    public void produce(final String topic, final Update update) {
        kafkaTemplate.send(topic, update);
    }
}
