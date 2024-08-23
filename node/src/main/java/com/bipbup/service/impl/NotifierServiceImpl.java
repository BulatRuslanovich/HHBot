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

@Slf4j
@RequiredArgsConstructor
@Service
public class NotifierServiceImpl implements NotifierService {

    private final APIHandler apiHandler;

    private final AnswerProducer answerProducer;

    private final ConfigService configService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("ru"));

    @Override
    @Scheduled(fixedRateString = "${notifier.period}")
    public void searchNewVacancies() {
        var page = 0;
        var sizeOfPage = 500;

        var configs = configService.getAll(page++, sizeOfPage);

        while (!configs.isEmpty()) {

            configs.stream()
                    .filter(this::isPresentQuery)
                    .forEach(this::processNewVacancies);

            configs = configService.getAll(page, sizeOfPage);
            page++;
        }
    }

    private boolean isPresentQuery(final AppUserConfig config) {
        return !(config.getQueryText() == null || config.getQueryText().isEmpty());
    }

    private void processNewVacancies(final AppUserConfig config) {
        var newVacancies = apiHandler.fetchNewVacancies(config);
        var user = config.getAppUser();

        if (!newVacancies.isEmpty()) {
            var lastNotificationTime = newVacancies.get(0).getPublishedAt().plusMinutes(1);
            Collections.reverse(newVacancies);

            newVacancies.forEach(v -> sendVacancyMessage(v, config));

            config.setLastNotificationTime(lastNotificationTime);
            configService.save(config);
        }

        log.info("For user {} find {} vacancies with config {}",
                user.getFirstName(), newVacancies.size(), config.getConfigName());
    }

    private void sendVacancyMessage(final VacancyDTO vacancy,
                                    final AppUserConfig config) {

        var message = String.format("""
                    *🔍 Название запроса:* %s
                    *💼 Вакансия:* %s
                    *👔 Работодатель:* %s
                    *🏙️ Регион:* %s
                    *🗓 Дата публикации:* %s
                    *🔗 Ссылка:* [Открыть вакансию](%s)
                    """,
                config.getConfigName(),
                vacancy.getNameVacancy(),
                vacancy.getNameEmployer(),
                vacancy.getNameArea(),
                vacancy.getPublishedAt().toLocalDate().format(formatter),
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
