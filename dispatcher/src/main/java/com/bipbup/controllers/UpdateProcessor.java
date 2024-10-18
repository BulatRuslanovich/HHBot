package com.bipbup.controllers;

import com.bipbup.config.KafkaTopicProperties;
import com.bipbup.easter.egg.EasterEggService;
import com.bipbup.exception.NullUpdateException;
import com.bipbup.service.UpdateProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateProcessor {

	private final UpdateProducer updateProducer;

	private final KafkaTopicProperties kafkaTopicProperties;

	private final EasterEggService easterEggService;

	private HeadHunterBot headHunterBot;

	public void registerBot(HeadHunterBot headHunterBot) {
		this.headHunterBot = headHunterBot;
	}

	public void processUpdate(Update update) throws NullPointerException {
		if (update == null) {
			log.error("Update is null");
			throw new NullUpdateException("Update is null");
		}

		if (update.hasMessage())
			processMessage(update);
		else if (update.hasCallbackQuery())
			processCallbackQuery(update);
		else
			logEmptyMessageUpdate(update);
	}

	private void logEmptyMessageUpdate(Update update) {
		if (update.getMyChatMember() == null) {
			log.warn("Chat member is null");
			return;
		}

		var status = update.getMyChatMember()
				.getNewChatMember()
				.getStatus();
		var user = update.getMyChatMember()
				.getFrom();

		if (status.equals("kicked")) {
			log.info("User {} block the bot", user.getFirstName());
			deactivateUser(user);
		} else if (status.equals("member")) {
			log.info("User {} joined", user.getFirstName());
		} else {
			log.error("Message is null");
		}
	}

	private void processMessage(Update update) {
		var message = update.getMessage();

		if (message.hasText()) {
			log.info("User {} wrote \"{}\"", message.getFrom()
					.getFirstName(), message.getText());

			easterEggService.getSendSticker(update)
					.ifPresent(this::sendStickerToTelegram);

			updateProducer.produce(kafkaTopicProperties.textUpdateTopic(), update);
		}
	}

	private void processCallbackQuery(Update update) {
		var callbackQuery = update.getCallbackQuery();

		log.info("User {} sent callback query with data: {}", callbackQuery.getFrom()
				.getFirstName(), callbackQuery.getData());
		updateProducer.produce(kafkaTopicProperties.callbackQueryUpdateTopic(), update);
	}

	public void sendToTelegram(BotApiMethod<?> method) {
		try {
			headHunterBot.execute(method);
		} catch (TelegramApiException e) {
			log.error("Error with send message execute", e);
		}
	}

	private void sendStickerToTelegram(SendSticker sticker) {
		try {
			headHunterBot.execute(sticker);
		} catch (TelegramApiException e) {
			log.error("Error with send message execute", e);
		}
	}

	private void deactivateUser(User user) {
		var callbackQuery = new CallbackQuery();
		callbackQuery.setFrom(user);
		callbackQuery.setData("deactivate_me");

		var update = new Update();
		update.setCallbackQuery(callbackQuery);

		updateProducer.produce(kafkaTopicProperties.callbackQueryUpdateTopic(), update);
	}
}
