package com.bipbup.sevice;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateProducer {
    void produce(String topic, Update update);
}
