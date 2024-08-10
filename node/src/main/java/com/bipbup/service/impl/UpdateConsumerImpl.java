package com.bipbup.service.impl;

import com.bipbup.service.MainService;
import com.bipbup.service.UpdateConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Service
public class UpdateConsumerImpl implements UpdateConsumer {
    private final MainService mainService;

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.text-update-topic}", groupId = "groupId")
    public void consumeTextUpdate(final Update update) {
        mainService.processMessage(update);
    }

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.callback-query-update-topic}", groupId = "groupId")
    public void consumeCallbackQueryUpdate(final Update update) {
        mainService.processCallbackQuery(update);
    }
}
