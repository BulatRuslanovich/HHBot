package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.CancellableStateHandler;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static com.bipbup.utils.CommandMessageConstants.AdminMessageTemplate.MESSAGE_SENT;


@Component
public class WaitBroadcastMessageHandler extends CancellableStateHandler {

    private final AnswerProducer producer;

    public WaitBroadcastMessageHandler(final UserService userService,
                                       final ConfigService configService,
                                       final BasicStateHandler basicStateHandler,
                                       final AnswerProducer producer) {
        super(userService, configService, basicStateHandler);
        this.producer = producer;
    }

    private void sendMessage(final AppUser user, final String output) {
        var message = SendMessage.builder()
                .text(output)
                .chatId(user.getTelegramId())
                .parseMode("MarkDown")
                .build();
        producer.produceAnswer(message);
    }

    private void sendBroadcast(final AppUser user, final String  input) {
        var users = userService.getAllUsers();
        users.stream().filter(u -> !u.getTelegramId().equals(user.getTelegramId()))
                .forEach(u -> sendMessage(u, input));
    }

    @Override
    public String process(final AppUser user, final String input) {
        if (isCancelCommand(input))
            return processCancelCommand(user);
        if (isBasicCommand(input))
            return processBasicCommand(user, input);

        sendBroadcast(user, input);
        userService.clearUserState(user.getTelegramId());

        return MESSAGE_SENT.getTemplate();
    }
}
