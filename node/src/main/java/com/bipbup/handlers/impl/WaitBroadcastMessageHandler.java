package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.CancellableStateHandler;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.ConfigService;
import com.bipbup.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static com.bipbup.handlers.impl.BasicStateHandler.ADMIN_LOG;
import static com.bipbup.utils.CommandMessageConstants.AdminMessageTemplate.MESSAGE_SENT;

@Slf4j
@Component
public class WaitBroadcastMessageHandler extends CancellableStateHandler {

    private final AnswerProducer producer;

    @Autowired
    public WaitBroadcastMessageHandler(UserService userService,
                                       ConfigService configService,
                                       BasicStateHandler basicStateHandler,
                                       AnswerProducer producer) {
        super(userService, configService, basicStateHandler);
        this.producer = producer;
    }

    @Override
    public String process(AppUser user, String input) {
        if (isCancelCommand(input))
            return processCancelCommand(user);
        if (isBasicCommand(input))
            return processBasicCommand(user, input);

        sendBroadcast(user, input);
        userService.clearUserState(user.getTelegramId());

        return MESSAGE_SENT.getTemplate();
    }

    private void sendMessage(AppUser user, String output) {
        var message = SendMessage.builder()
                .text(output)
                .chatId(user.getTelegramId())
                .parseMode("MarkDown")
                .build();
        producer.produceAnswer(message);
    }

    private void sendBroadcast(AppUser user, String  input) {
        var users = userService.getAllUsers();
        log.info(ADMIN_LOG, "{} send message to everyone:\n{}", user.getFirstName(), input);
        users.stream().filter(u -> !u.getTelegramId().equals(user.getTelegramId()))
                .forEach(u -> sendMessage(u, input));
    }
}
