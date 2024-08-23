package com.bipbup.service;

import com.bipbup.wrapper.impl.EditMessageWrapper;
import com.bipbup.wrapper.impl.SendMessageWrapper;

public interface AnswerConsumer {

    void consumeSendMessage(SendMessageWrapper sendMessage);

    void consumeEditMessage(EditMessageWrapper editMessage);
}
