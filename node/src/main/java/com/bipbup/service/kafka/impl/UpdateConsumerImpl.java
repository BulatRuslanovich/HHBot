package com.bipbup.service.kafka.impl;

import com.bipbup.service.bot.MainService;
import com.bipbup.service.kafka.UpdateConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Service
public class UpdateConsumerImpl implements UpdateConsumer {

    private final MainService mainService;

    @Override
    @KafkaListener(topics = "${topics.text-update-topic}", groupId = "groupId")
    public void consumeTextUpdate(Update update) {
        mainService.processMessage(update);
    }

    @Override
    @KafkaListener(topics = "${topics.callback-query-update-topic}", groupId = "groupId")
    public void consumeCallbackQueryUpdate(Update update) {
        mainService.processCallbackQuery(update);
    }
}
