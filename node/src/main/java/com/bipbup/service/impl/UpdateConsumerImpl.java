package com.bipbup.service.impl;

import com.bipbup.service.MainService;
import com.bipbup.service.NotifierService;
import com.bipbup.service.UpdateConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Log4j
@RequiredArgsConstructor
@Service
public class UpdateConsumerImpl implements UpdateConsumer {
    private final MainService mainService;

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.text-update-topic}", groupId = "groupId")
    public void consumeTextUpdate(Update update) {
        log.debug("Update received");
        mainService.processMessage(update);
    }
}
