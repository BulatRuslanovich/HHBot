package com.bipbup.service.bot.impl;

import com.bipbup.dto.VacancyDTO;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.service.api.APIHandler;
import com.bipbup.service.bot.NotifierService;
import com.bipbup.service.db.ConfigService;
import com.bipbup.service.kafka.AnswerProducer;
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

	private final APIHandler apiHandler;

	private final AnswerProducer answerProducer;

	private final ConfigService configService;

	@Override
	@Scheduled(fixedRateString = "${notifier.period}")
	public void searchNewVacancies() {
		var page = 0;

		var configs = configService.getConfigsFromPage(page++, SIZE_OF_PAGE);

		while (!configs.isEmpty()) {
			configs.parallelStream()
					.filter(this::isPresentQuery)
					.forEach(this::processNewVacancies);

			configs = configService.getConfigsFromPage(page, SIZE_OF_PAGE);
			page++;
		}
	}

	private boolean isPresentQuery(AppUserConfig config) {
		return !(config.getQueryText() == null || config.getQueryText()
				.isEmpty());
	}

	private void processNewVacancies(AppUserConfig config) {
		var newVacancies = apiHandler.fetchNewVacancies(config);
		var user = config.getAppUser();

		if (!newVacancies.isEmpty()) {
			var lastNotificationTime = newVacancies.get(0).getPublishedAt()
					.plusMinutes(1);
			Collections.reverse(newVacancies);

			log.debug("Sending {} vacancies to user {} with config {}", newVacancies.size(), user.getFirstName(), config.getConfigName());

			newVacancies.forEach(vacancy -> sendVacancyMessage(vacancy, config));

			config.setLastNotificationTime(lastNotificationTime);
			configService.saveConfig(config);
		}

		log.info("For user {} find {} vacancies with config {}", user.getFirstName(), newVacancies.size(), config.getConfigName());
	}

	private void sendVacancyMessage(VacancyDTO vacancy, AppUserConfig config) {
		var configName = config.getConfigName();
		var jobTitle = vacancy.getNameVacancy();
		var employer = vacancy.getNameEmployer();
		var region = vacancy.getNameArea();
		var publishDate = vacancy.getPublishedAt().toLocalDate().format(FORMATTER);
		var roles = vacancy.getProfessionalRoles();
		var experience = vacancy.getExperience();
		var employmentType = vacancy.getEmployment();
		var workSchedule = vacancy.getSchedule();
		var jobLink = vacancy.getUrl();

		var message =
				String.join("\n", String.format("<i>• Название запроса:</i> <b>%s</b>", configName),
				            String.format("• <i>Вакансия:</i> <b>%s</b>", jobTitle),
				            String.format("• <i>Работодатель:</i> <b>%s</b>", employer),
				            String.format("• <i>Регион:</i> <b>%s</b>", region),
				            String.format("• <i>Дата:</i> <b>%s</b>", publishDate),
				            String.format("• <i>Роль:</i> <b>%s</b>", roles),
				            String.format("• <i>Опыт:</i> <b>%s</b>", experience),
				            String.format("• <i>Тип занятости:</i> <b>%s</b>", employmentType),
				            String.format("• <i>График:</i> <b>%s</b>", workSchedule),
				            String.format("[• Ссылка](%s)", jobLink));


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
