package com.bipbup.easter.egg;

import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Service
public class EasterEggService {

	private static final Map<String, String> wordToStickerId = Map.of(
			"java" , "CAACAgIAAxkBAAIFPmbI3jhL4EduWvGuPRYcLQZoZaXJAAK2RwACw115S9Ixcad37Jb7NQQ",
			"c++" , "CAACAgIAAxkBAAIFUWbI4zy9Hz75c2HAQ4ktbM5-yGaiAAK9PAACxih4Sy04iZSeWyG0NQQ",
			"javascript" , "CAACAgIAAxkBAAIFdmbI5bDHR6rHgpLIXtLtIPy8ro-tAAL2QQACctF4S6_e0ZZv1pzyNQQ",
			"python" , "CAACAgIAAxkBAAIFd2bI5hnwHgT_BL5jTZtoeT1aL9JwAALISAAC5PF5S7Se8n5ySpqANQQ",
			"c#" , "CAACAgIAAxkBAAIFeGbI5kjF58JJGk4yeE-hYI6RwyvuAAJfQwACJad4SypZPWXZRAYeNQQ",
			"котик" , "CAACAgIAAxkBAAIFeWbI5mG6zqA00c19q65qlyCqqJE2AAJ4FAACiQ5BS8wYzPDMMcXINQQ"
	);

	public Optional<SendSticker> getSendSticker(Update update) {
		var message = update.getMessage();
		var input = message.getText().toLowerCase();
		var firstName = update.getMessage().getFrom().getFirstName();
		var stickerId = wordToStickerId.get(input);

		if (stickerId != null) {
			log.info("User {} activates Easter egg by writing \"{}\"", firstName, input);
			return Optional.of(createSticker(stickerId, message.getChatId()));
		}

		return Optional.empty();
	}

	private SendSticker createSticker(String stickerId, long chatId) {
		return SendSticker.builder()
				.sticker(new InputFile(stickerId))
				.chatId(chatId)
				.build();
	}
}
