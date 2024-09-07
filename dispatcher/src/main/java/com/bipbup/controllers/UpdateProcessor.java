package com.bipbup.controllers;

import com.bipbup.service.UpdateProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

@Slf4j
@RequiredArgsConstructor
@Component
public class UpdateProcessor {

    private static final Marker EASTER_EGG_MARKER = MarkerFactory.getMarker("EASTER_EGG");

    private static final Marker WELCOME_MARKER = MarkerFactory.getMarker("WELCOME");

    private static final Marker BAD_HUMAN_MARKER = MarkerFactory.getMarker("BAD_HUMAN");

    private final UpdateProducer updateProducer;

    @Value("${spring.kafka.topics.text-update-topic}")
    private String textUpdateTopic;

    @Value("${spring.kafka.topics.callback-query-update-topic}")
    private String callbackQueryUpdateTopic;

    private MyTelegramBot myTelegramBot;

    private void logEmptyMessageUpdate(final Update update) {
        var status = update.getMyChatMember().getNewChatMember().getStatus();
        var user = update.getMyChatMember().getFrom();
        if (status.equals("kicked")) {
            log.info(BAD_HUMAN_MARKER, "User {} block the bot", user.getFirstName());
            deactivateUser(user);
        } else if (status.equals("member")) {
            log.info(WELCOME_MARKER, "User {} joined", user.getFirstName());
        } else {
            log.error("Message is null");
        }
    }

    private void deactivateUser(User user) {
        var callbackQuery = new CallbackQuery();
        callbackQuery.setFrom(user);
        callbackQuery.setData("delete_me_from_db");

        var update = new Update();
        update.setCallbackQuery(callbackQuery);

        updateProducer.produce(callbackQueryUpdateTopic, update);
    }

    public void registerBot(final MyTelegramBot myTelegramBot) {
        this.myTelegramBot = myTelegramBot;
    }

    public boolean processUpdate(final Update update) {
        if (update == null) {
            log.error("Update is null");
            return false;
        }

        if (update.hasMessage()) {
            processMessage(update);
        } else if (update.hasCallbackQuery()) {
            processCallbackQuery(update);
        } else {
            logEmptyMessageUpdate(update);
        }

        return true;
    }

    private void processMessage(Update update) {
        var message = update.getMessage();

        if (message.hasText()) {
            log.info("User {} wrote \"{}\"", message.getFrom().getFirstName(), message.getText());
            easterEgg(update);
            updateProducer.produce(textUpdateTopic, update);
        }
    }

    private void processCallbackQuery(final Update update) {
        var callbackQuery = update.getCallbackQuery();

        log.info("User {} sent callback query with data: {}",
                callbackQuery.getFrom().getFirstName(), callbackQuery.getData());
        updateProducer.produce(callbackQueryUpdateTopic, update);
    }

    public void setView(final SendMessage message) {
        try {
            myTelegramBot.execute(message);
        } catch (TelegramApiRequestException e) {
            if (e.getErrorCode() == 403 && e.getApiResponse().contains("bot was blocked by the user")) {
                log.info("Bot was blocked by user: {}", message.getChatId());
            }
        } catch (TelegramApiException e) {
            log.error("Error with send message execute", e);
        }
    }


    public void setEdit(final EditMessageText message) {
        try {
            myTelegramBot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error with edit message execute", e);
        }
    }

    private void easterEgg(Update update) {
        var message = update.getMessage();
        var input = message.getText();
        var firstName = update.getMessage().getFrom().getFirstName();

        String stickerId = switch (input.toLowerCase()) {
            case "java" -> "CAACAgIAAxkBAAIFPmbI3jhL4EduWvGuPRYcLQZoZaXJAAK2RwACw115S9Ixcad37Jb7NQQ";
            case "c++" -> "CAACAgIAAxkBAAIFUWbI4zy9Hz75c2HAQ4ktbM5-yGaiAAK9PAACxih4Sy04iZSeWyG0NQQ";
            case "javascript" -> "CAACAgIAAxkBAAIFdmbI5bDHR6rHgpLIXtLtIPy8ro-tAAL2QQACctF4S6_e0ZZv1pzyNQQ";
            case "python" -> "CAACAgIAAxkBAAIFd2bI5hnwHgT_BL5jTZtoeT1aL9JwAALISAAC5PF5S7Se8n5ySpqANQQ";
            case "c#" -> "CAACAgIAAxkBAAIFeGbI5kjF58JJGk4yeE-hYI6RwyvuAAJfQwACJad4SypZPWXZRAYeNQQ";
            case "котик" -> "CAACAgIAAxkBAAIFeWbI5mG6zqA00c19q65qlyCqqJE2AAJ4FAACiQ5BS8wYzPDMMcXINQQ";
            default -> null;
        };

        if (stickerId != null) {
            log.info(EASTER_EGG_MARKER, "User {} activates Easter egg by writing \"{}\"", firstName, input);
            createAndSendSticker(stickerId, message);
        }
    }

    private void createAndSendSticker(String stickerId, Message message) {
        var sticker = SendSticker.builder()
                .sticker(new InputFile(stickerId))
                .chatId(message.getChatId())
                .build();

        try {
            myTelegramBot.execute(sticker);
        } catch (TelegramApiException e) {
            log.error("Error with send sticker execute", e);
        }
    }
}
