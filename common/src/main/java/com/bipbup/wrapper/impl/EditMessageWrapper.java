package com.bipbup.wrapper.impl;

import com.bipbup.wrapper.MessageWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

@Getter
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class EditMessageWrapper implements MessageWrapper {

    private EditMessageText message;
}
