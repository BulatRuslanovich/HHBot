package com.bipbup.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateConsumer {

    @SuppressWarnings("unused")
    void consumeTextUpdate(Update update);

    @SuppressWarnings("unused")
    void consumeCallbackQueryUpdate(Update update);
}
