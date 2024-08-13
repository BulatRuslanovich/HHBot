package com.bipbup.service.impl;

import com.bipbup.dto.VacancyDTO;
import com.bipbup.entity.AppUser;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.service.APIHandler;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.ConfigService;
import com.bipbup.service.NotifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotifierServiceImpl implements NotifierService {

    private final APIHandler apiHandler;

    private final AnswerProducer answerProducer;

    private final ConfigService configService;

    @Override
    @Scheduled(fixedRateString = "${notifier.period}")
    public void searchNewVacancies() {
        int page = 0;
        int sizeOfPage = 50;

        List<AppUserConfig> configs = configService.getAll(page, sizeOfPage);

        while (!configs.isEmpty()) {
            for (var config : configs) {
                if (config.getQueryText() == null
                        || config.getQueryText().isEmpty()) {
                    continue;
                }

                processNewVacancies(config);
            }

            configs = configService.getAll(++page, sizeOfPage);
        }
    }

    private void processNewVacancies(final AppUserConfig appUserConfig) {
        List<VacancyDTO> newVacancies =
                apiHandler.getNewVacancies(appUserConfig);
        var appUser = appUserConfig.getAppUser();

        if (!newVacancies.isEmpty()) {
            var lastNotificationTime =
                    newVacancies.get(0).getPublishedAt().plusMinutes(1);
            Collections.reverse(newVacancies);

            for (var vacancy : newVacancies) {
                sendVacancyMessage(vacancy, appUser);
            }

            appUserConfig.setLastNotificationTime(lastNotificationTime);
            configService.save(appUserConfig);
        }

        log.info("For user {} find {} vacancies with config {}",
                appUser.getFirstName(),
                newVacancies.size(),
                appUserConfig.getConfigName());
    }

    private void sendVacancyMessage(final VacancyDTO newVacancyDTO,
                                    final AppUser appUser) {
        String message = String.format("""
                        *Вакансия:* %s
                        *Работодатель:* %s
                        *Город:* %s
                        *Дата публикации:* %s
                        *Ссылка:* %s
                        """,
                newVacancyDTO.getNameVacancy(),
                newVacancyDTO.getNameEmployer(),
                newVacancyDTO.getNameArea(),
                newVacancyDTO.getPublishedAt().toLocalDate(),
                newVacancyDTO.getUrl());


        SendMessage sendMessage = SendMessage.builder()
                .text(message)
                .parseMode("Markdown")
                .chatId(appUser.getTelegramId())
                .build();

        answerProducer.produceAnswer(sendMessage);
    }
}
