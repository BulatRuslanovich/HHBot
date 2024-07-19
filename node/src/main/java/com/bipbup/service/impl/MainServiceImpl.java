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

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class MainServiceImpl implements MainService {
    private final AppUserDAO appUserDAO;
    private final AnswerProducer answerProducer;

    @Override
    public void processMessage(Update update) {
        AppUser appUser = findOrSaveAppUser(update);

        String text = "Dude, find a job already...";
        sendAnswer(text, appUser.getTelegramId());
    }

    private void startInteraction(AppUser appUser) {
        String text = "Welcome to capitalism, " + appUser.getFirstName() + "!";
        sendAnswer(text, appUser.getTelegramId());
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
                    .firstLoginDate(LocalDateTime.now())
                    .username(messageSender.getUserName())
                    .firstName(messageSender.getFirstName())
                    .lastName(messageSender.getLastName())
                    .lastNotificationTime(LocalDateTime.now())
                    .build();

            startInteraction(appUser);
            return appUserDAO.save(appUser);
        }

        return appUserOptional.get();
    }
}
