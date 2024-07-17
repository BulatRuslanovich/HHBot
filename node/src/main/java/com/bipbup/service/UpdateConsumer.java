package com.bipbup.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateConsumer {
    void consumeTextUpdate(Update update);
}
