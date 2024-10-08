package com.bipbup.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public interface AnswerProducer {

    void produceAnswer(final SendMessage sendMessage);

    void produceEdit(final EditMessageText editMessage);
}
