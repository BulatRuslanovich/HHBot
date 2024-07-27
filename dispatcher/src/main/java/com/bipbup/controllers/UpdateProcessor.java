package com.bipbup.controllers;

import com.bipbup.service.UpdateProducer;
import com.bipbup.utils.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class UpdateProcessor {
    @Value("${spring.kafka.topics.text-update-topic}")
    private String textUpdateTopic;
    private MyTelegramBot myTelegramBot;
    private final MessageUtil messageUtil;
    private final UpdateProducer updateProducer;

    public void registerBot(MyTelegramBot myTelegramBot) {
        this.myTelegramBot = myTelegramBot;
        setCommandsMenu(setMenuCommands());
    }

    private SetMyCommands setMenuCommands() {
        var commands = List.of(
                new BotCommand("set_query", "Задать запрос"),
                new BotCommand("set_experience", "Выбрать опыт")
        );

        return new SetMyCommands(commands, new BotCommandScopeDefault(), null);
    }


    public void processUpdate(Update update) {
        if (update == null) {
            log.error("Update is null");
            return;
        }

        if (update.hasMessage()) {
            processMessage(update);
        } else {
            log.error("Message is null");
        }
    }

    private void processMessage(Update update) {
        var message = update.getMessage();

        if (message.hasText()) {
            log.info(message.getText());
            updateProducer.produce(textUpdateTopic, update);
        } else {
            setUnsupportedMessageType(update);
        }
    }

    private void setUnsupportedMessageType(Update update) {
        var sendMessage = messageUtil.generateSendMessage(update, "Неподдерживаемый тип данных!");
        setView(sendMessage);
    }

    public void setView(SendMessage sendMessage) {
        myTelegramBot.sendAnswerMessage(sendMessage);
    }

    private void setCommandsMenu(SetMyCommands commands) {
        try {
            myTelegramBot.execute(commands);
        } catch (TelegramApiException e) {
            log.error("Error with commands execute");
        }
    }
}
