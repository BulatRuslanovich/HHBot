package com.bipbup.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface MainService {

    void processMessage(final Update update);

    void processCallbackQuery(final Update update);
}
