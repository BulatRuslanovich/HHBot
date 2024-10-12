package com.bipbup.handlers.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.handlers.StateHandler;
import static com.bipbup.handlers.impl.BasicStateHandler.ADMIN_LOG;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.UserService;
import com.bipbup.service.cache.UserStateCacheService;
import static com.bipbup.utils.CommandMessageConstants.AdminMessageTemplate.MESSAGE_SENT;
import com.bipbup.utils.HandlerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class WaitBroadcastMessageHandler implements StateHandler {

    private final AnswerProducer producer;

    private final UserStateCacheService userStateCacheService;

    private final UserService userService;

    private final HandlerUtils handlerUtils;

    @Override
    public String process(AppUser user, String input) {
        if (handlerUtils.isCancelCommand(input))
            return handlerUtils.processCancelCommand(user);
        if (handlerUtils.isBasicCommand(input))
            return handlerUtils.processBasicCommand(user, input);

        sendBroadcast(user, input);
        userStateCacheService.clearUserState(user.getTelegramId());

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
        var users = userService.getAppUsers();
        log.info(ADMIN_LOG, "{} send message to everyone:\n{}", user.getFirstName(), input);
        users.stream().filter(u -> !u.getTelegramId().equals(user.getTelegramId()))
                .forEach(u -> sendMessage(u, input));
    }
}
