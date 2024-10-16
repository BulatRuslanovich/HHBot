package com.bipbup.service.bot.impl;

import com.bipbup.dto.Vacancy;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.service.api.VacancyService;
import com.bipbup.service.bot.NotifierService;
import com.bipbup.service.db.ConfigService;
import com.bipbup.service.kafka.AnswerProducer;
import com.bipbup.utils.DateTimeUtil;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotifierServiceImpl implements NotifierService {

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("ru"));

	private static final int SIZE_OF_PAGE = 1000;

	private final AnswerProducer answerProducer;

	private final VacancyService vacancyService;

	private final ConfigService configService;

	@Override
	@Scheduled(fixedRateString = "${notifier.period}")
	public void searchNewVacancies() {
		var page = 0;

		var configs = configService.getConfigsFromPage(page++, SIZE_OF_PAGE);

		while (!configs.isEmpty()) {
			configs.stream()
					.filter(this::queryExist)
					.forEach(this::processNewVacancies);

			configs = configService.getConfigsFromPage(page, SIZE_OF_PAGE);
			page++;
		}
	}

	private boolean queryExist(AppUserConfig config) {
		return !(config.getQueryText() == null || config.getQueryText()
				.isEmpty());
	}

	private void processNewVacancies(AppUserConfig config) {
		var vacancies = vacancyService.fetchNewVacancies(config);
		var user = config.getAppUser();

		if (!vacancies.isEmpty()) {
			var lastNotificationTime = DateTimeUtil.convertToDate(vacancies.get(0).publishedAt()).plusMinutes(1);
			Collections.reverse(vacancies);

			vacancies.forEach(vacancy -> sendVacancyMessage(vacancy, config));

			config.setLastNotificationTime(lastNotificationTime);
			configService.saveConfig(config);
		}

		log.info("For user {} find {} vacancies with config {}", user.getFirstName(), vacancies.size(), config.getConfigName());
	}

	private void sendVacancyMessage(Vacancy vacancy, AppUserConfig config) {
		var configName = config.getConfigName();
		var jobTitle = vacancy.name();
		var employer = vacancy.employer().name();
		var region = vacancy.area().name();
		var publishDate = DateTimeUtil.convertToDate(vacancy.publishedAt()).format(FORMATTER);

		var roles = vacancy.professionalRoles().get(0).name();
		var experience = vacancy.experience().name();
		var employmentType = vacancy.employment().name();
		var workSchedule = vacancy.schedule().name();
		var jobLink = vacancy.alternateUrl();
		var trusted = vacancy.employer().trusted() ? "✅ Проверенный" : "\uD83E\uDD28 Сомнительный";

		var message =
				String.join("\n", String.format("<i>• Конфигурация:</i> <b>%s</b>", configName),
				            String.format("• <i>Вакансия:</i> <b>%s</b>", jobTitle),
				            String.format("• <i>Работодатель:</i> <b>%s</b> %s", employer, trusted),
				            String.format("• <i>Регион:</i> <b>%s</b>", region),
				            String.format("• <i>Дата:</i> <b>%s</b>", publishDate),
				            String.format("• <i>Роль:</i> <b>%s</b>", roles),
				            String.format("• <i>Опыт:</i> <b>%s</b>", experience),
				            String.format("• <i>Тип занятости:</i> <b>%s</b>", employmentType),
				            String.format("• <i>График:</i> <b>%s</b>", workSchedule),
				            String.format("• (Ссылка)[%s]", jobLink));

		var telegramId = config.getAppUser()
				.getTelegramId();

		var sendMessage = SendMessage.builder()
				.text(message)
				.parseMode(ParseMode.HTML)
				.chatId(telegramId)
				.build();

		answerProducer.produceAnswer(sendMessage);
	}
}
