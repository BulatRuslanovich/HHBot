package com.bipbup.service.impl;

import com.bipbup.dao.AppUserConfigDAO;
import com.bipbup.dto.Vacancy;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.service.APIHandler;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.NotifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotifierServiceImpl implements NotifierService {
    private final APIHandler apiHandler;
    private final AnswerProducer answerProducer;
    private final AppUserConfigDAO appUserConfigDAO;

    @Override
    @Scheduled(fixedRateString = "${notifier.period}")
    public void searchNewVacancies() {
        var appUserConfigs = appUserConfigDAO.findAll();

        for (var appUserConfig : appUserConfigs) {
            if (Objects.isNull(appUserConfig.getQueryText()) || appUserConfig.getQueryText().isEmpty())
                continue;

            processNewVacancies(appUserConfig);
        }
    }

    private void processNewVacancies(AppUserConfig appUserConfig) {
        List<Vacancy> newVacancies = apiHandler.getNewVacancies(appUserConfig);
        AppUser appUser = appUserConfig.getAppUser();

        if (!newVacancies.isEmpty()) {
            LocalDateTime lastNotificationTime = newVacancies.get(0).getPublishedAt().plusMinutes(1);
            Collections.reverse(newVacancies);

            for (var vacancy : newVacancies) {
                sendVacancyMessage(vacancy, appUser);
            }

            appUserConfig.setLastNotificationTime(lastNotificationTime);
            appUserConfigDAO.save(appUserConfig);
        }

        log.info("For user {} find {} vacancies with config {}",
                appUser.getFirstName(), newVacancies.size(), appUserConfig.getConfigName());
    }

    private void sendVacancyMessage(Vacancy newVacancy, AppUser appUser) {
        String message = String.format("""
                        *Вакансия:* %s
                        *Работодатель:* %s
                        *Город:* %s
                        *Дата публикации:* %s
                        *Ссылка:* %s
                        """,
                newVacancy.getNameVacancy(),
                newVacancy.getNameEmployer(),
                newVacancy.getNameArea(),
                newVacancy.getPublishedAt().toLocalDate(),
                newVacancy.getUrl());


        SendMessage sendMessage = SendMessage.builder()
                .text(message)
                .parseMode("Markdown")
                .chatId(appUser.getTelegramId())
                .build();

        answerProducer.produceAnswer(sendMessage);
    }
}
