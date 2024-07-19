package com.bipbup.service.impl;

import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;


@RequiredArgsConstructor
@Service
public class MainServiceImpl implements MainService {
    private final AppUserDAO appUserDAO;
    private final AnswerProducer answerProducer;

    @Override
    public void processMessage(Update update) {
        var appUser = findOrSaveAppUser(update);
//        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        if (text.equals("/start")) {
            output = startInteraction(appUser);
        } else if (text.equals("/help")) {
            output = helpOutput(appUser);
        }

        sendAnswer(output, appUser.getTelegramId());
    }

    private String helpOutput(AppUser appUser) {
        return """
                Вот команды бота, дорогой друг, %s:
                /start - для того чтобы бот стартанул
                /help - вызывает данную строку
                """.formatted(appUser.getUsername());
    }

    private String startInteraction(AppUser appUser) {
        return "Добро пожаловать в капитализм %s!".formatted(appUser.getUsername());
    }

    private void sendAnswer(String text, Long chatId) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        answerProducer.produceAnswer(sendMessage);
    }

    private AppUser findOrSaveAppUser(Update update) {
        final User messageSender = update.getMessage().getFrom();
        var appUserOptional = appUserDAO.findByTelegramId(messageSender.getId());

        boolean isAppUserExist = appUserOptional.isPresent();
        if (!isAppUserExist) {
            AppUser appUser = AppUser.builder()
                    .telegramId(messageSender.getId())
                    .username(messageSender.getUserName())
                    .firstName(messageSender.getFirstName())
                    .lastName(messageSender.getLastName())
                    .build();

            return appUserDAO.save(appUser);
        }

        return appUserOptional.get();
    }
}
