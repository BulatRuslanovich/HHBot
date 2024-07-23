package com.bipbup.service.impl;

import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.model.Vacancy;
import com.bipbup.service.APIHandler;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.NotifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Collections;
import java.util.List;

@Log4j
@RequiredArgsConstructor
@Service
public class NotifierServiceImpl implements NotifierService {
    private final APIHandler apiHandler;
    private final AnswerProducer answerProducer;
    private final AppUserDAO appUserDAO;

    @Scheduled(fixedRateString = "${notifier.period}")
    @Override
    public void informAboutNewVacancies() {
        log.info("Start vacancies searching...");

        var users = appUserDAO.findAll();

        for (var user : users) {
            if (user.getQueryText() == null || user.getQueryText().isEmpty()) {
                continue;
            }

            List<Vacancy> newVacancies = apiHandler.getListWithNewVacancies(user);
            Collections.reverse(newVacancies);

            if (!newVacancies.isEmpty()) {
                for (var newVacancy : newVacancies) {
                    user.setLastNotificationTime(newVacancy.getPublishedAt().plusMinutes(1));
                    appUserDAO.save(user);
                    createMessageWithVacancy(newVacancy, user);
                }
            }

            log.info("For user %s find %d vacancies".formatted(user.getFirstName(), newVacancies.size()));
        }


        log.info("Stop vacancies searching...");
    }

    private void createMessageWithVacancy(Vacancy newVacancy, AppUser appUser) {
        String message = newVacancy.getNameVacancy() + "\n" +
                         newVacancy.getNameEmployer() + "\n" +
                         newVacancy.getNameArea() + "\n" +
                         newVacancy.getPublishedAt().toString() + "\n" +
                         newVacancy.getUrl() + "\n";

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(message);
        sendMessage.setChatId(appUser.getTelegramId().toString());

        answerProducer.produceAnswer(sendMessage);
    }
}
