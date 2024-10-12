package com.bipbup.service.impl;

import com.bipbup.dto.VacancyDTO;
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

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;

import static com.bipbup.utils.CommandMessageConstants.MessageTemplate.VACANCY;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotifierServiceImpl implements NotifierService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("ru"));

    private static final int SIZE_OF_PAGE = 500;

    private final APIHandler apiHandler;

    private final AnswerProducer answerProducer;

    private final ConfigService configService;


    @Override
    @Scheduled(fixedRateString = "${notifier.period}")
    public void searchNewVacancies() {
        var page = 0;

        var configs = configService.getConfigsFromPage(page++, SIZE_OF_PAGE);

        while (!configs.isEmpty()) {

            configs.stream()
                    .filter(this::isPresentQuery)
                    .forEach(this::processNewVacancies);

            configs = configService.getConfigsFromPage(page, SIZE_OF_PAGE);
            page++;
        }
    }

    private boolean isPresentQuery(AppUserConfig config) {
        return !(config.getQueryText() == null || config.getQueryText().isEmpty());
    }

    private void processNewVacancies(AppUserConfig config) {
        var newVacancies = apiHandler.fetchNewVacancies(config);
        var user = config.getAppUser();

        if (!newVacancies.isEmpty()) {
            var lastNotificationTime = newVacancies.get(0).getPublishedAt().plusMinutes(1);
            Collections.reverse(newVacancies);

            newVacancies.forEach(v -> sendVacancyMessage(v, config));

            config.setLastNotificationTime(lastNotificationTime);
            configService.saveConfig(config, false);
        }

        log.info("For user {} find {} vacancies with config {}",
                user.getFirstName(), newVacancies.size(), config.getConfigName());
    }

    private void sendVacancyMessage(VacancyDTO vacancy, AppUserConfig config) {
        var message = String.format(VACANCY.getTemplate(),
                config.getConfigName(),
                vacancy.getNameVacancy(),
                vacancy.getNameEmployer(),
                vacancy.getNameArea(),
                vacancy.getPublishedAt().toLocalDate().format(FORMATTER),
                vacancy.getUrl());

        var telegramId = config.getAppUser().getTelegramId();

        var sendMessage = SendMessage.builder()
                .text(message)
                .parseMode("Markdown")
                .chatId(telegramId)
                .build();

        answerProducer.produceAnswer(sendMessage);
    }
}
