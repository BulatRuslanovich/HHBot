package com.bipbup.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

public interface AnswerConsumer {
    void consumeSendMessage(SendMessage sendMessage);
    void consumeEditMessage(EditMessageText editMessage);
    void consumeDeleteMessage(DeleteMessage deleteMessage);
}
