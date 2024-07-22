package com.bipbup.service.impl;

import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.time.LocalDateTime;

import static com.bipbup.enums.AppUserState.BASIC_STATE;
import static com.bipbup.enums.AppUserState.WAIT_QUERY_STATE;


@Log4j
@RequiredArgsConstructor
@Service
public class MainServiceImpl implements MainService {
    private final AppUserDAO appUserDAO;
    private final AnswerProducer answerProducer;

    @Override
    public void processMessage(Update update) {
        var appUser = findOrSaveAppUser(update);
        var userState = appUser.getState();
        var text = update.getMessage().getText();
        var output = "";

        if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_QUERY_STATE.equals(userState)) {
            appUser.setQueryText(text); //TODO: make validation
            appUser.setLastNotificationTime(LocalDateTime.now().minusDays(4));
            appUser.setState(BASIC_STATE);
            appUserDAO.save(appUser);
            log.info("User %s set query \"%s\"".formatted(appUser.getUsername(), text));
            output = "Запрос успешно изменен";
        }

        if (!output.isEmpty()) sendAnswer(output, appUser.getTelegramId());
    }

    private String processServiceCommand(AppUser appUser, String text) {
        return switch (text) {
            case "/start" -> startInteraction(appUser);
            case "/help" -> helpOutput(appUser);
            case "/choose_query" -> chooseQueryOutput(appUser);
            default -> "";
        };
    }

    private String chooseQueryOutput(AppUser appUser) {
        appUser.setState(WAIT_QUERY_STATE);
        appUserDAO.save(appUser);
        return "Введите запрос";
    }


    private String helpOutput(AppUser appUser) {
        return """
                Вот команды бота, дорогой друг, %s:
                /start - для того чтобы бот стартанул
                /help - вызывает данную строку
                /choose_query - задает нужный вам запрос
                """.formatted(appUser.getUsername());
    }

    private String startInteraction(AppUser appUser) {
        return "Добро пожаловать в капитализм %s!".formatted(appUser.getUsername());
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
                    .username(messageSender.getUserName())
                    .firstName(messageSender.getFirstName())
                    .state(BASIC_STATE)
                    .lastName(messageSender.getLastName())
                    .build();

            return appUserDAO.save(appUser);
        }

        return appUserOptional.get();
    }
}
