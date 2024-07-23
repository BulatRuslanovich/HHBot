package com.bipbup.service.impl;

import com.bipbup.dao.AppUserDAO;
import com.bipbup.entity.AppUser;
import com.bipbup.enums.ExperienceParam;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDateTime;
import java.util.List;

import static com.bipbup.enums.AppUserState.*;


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
        ReplyKeyboard replyKeyboard;

        if (BASIC_STATE.equals(userState)) {
            output = processServiceCommand(appUser, text);
        } else if (WAIT_QUERY_STATE.equals(userState)) {
            output = processQueryText(appUser, text);
        } else if (WAIT_EXPERIENCE_STATE.equals(userState)) {
            output = processExperience(appUser, text);
        }

        if (!output.isEmpty()) {
            if (appUser.getState().equals(WAIT_EXPERIENCE_STATE)) {
                replyKeyboard = getExperienceKeyboard();
                sendAnswer(output, appUser.getTelegramId(), replyKeyboard);
            } else {
                sendAnswer(output, appUser.getTelegramId());
            }
        }
    }

    private String processQueryText(AppUser appUser, String text) {
        text = text.replace("+", "%2B");
        appUser.setQueryText(text); //TODO: make validation
        appUser.setLastNotificationTime(LocalDateTime.now().minusDays(3)); //TODO: make better logic
        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);
        log.info("User %s set query \"%s\"".formatted(appUser.getUsername(), text));
        return "Запрос успешно изменен";
    }

    private String processExperience(AppUser appUser, String text) {
        switch (text) {
            case "Без опыта" -> appUser.setExperience(ExperienceParam.NO_EXPERIENCE);
            case "1-3 года" -> appUser.setExperience(ExperienceParam.BETWEEN_1_AND_3);
            case "3-6 лет" -> appUser.setExperience(ExperienceParam.BETWEEN_3_AND_6);
            case "Более 6 лет" -> appUser.setExperience(ExperienceParam.MORE_THEN_6);
            default -> appUser.setExperience(ExperienceParam.NO_MATTER);
        }

        appUser.setState(BASIC_STATE);
        appUserDAO.save(appUser);

        return appUser.getExperience() == ExperienceParam.NO_MATTER ? "Опыт работы не будет фильтроваться"
                : "Опыт работы успешно установлен (%s)".formatted(text);
    }

    private String processServiceCommand(AppUser appUser, String text) {
        return switch (text) {
            case "/start" -> startInteraction(appUser);
            case "/help" -> helpOutput(appUser);
            case "/choose_query" -> chooseQueryOutput(appUser);
            case "/choose_exp" -> chooseExpOutput(appUser);
            default -> "";
        };
    }

    private String chooseExpOutput(AppUser appUser) {
        appUser.setState(WAIT_EXPERIENCE_STATE);
        appUserDAO.save(appUser);
        return "Выберите опыт работы";
    }

    private ReplyKeyboard getExperienceKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add("Без опыта");
        row1.add("1-3 года");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("3-6 лет");
        row2.add("Более 6 лет");

        KeyboardRow row3 = new KeyboardRow();
        row3.add("Без фильтра");

        return new ReplyKeyboardMarkup(List.of(row1, row2, row3));
    }

    private String chooseQueryOutput(AppUser appUser) {
        appUser.setState(WAIT_QUERY_STATE);
        appUserDAO.save(appUser);
        return "Введите запрос";
    }


    private String helpOutput(AppUser appUser) {
        return """
                Вот команды бота, дорогой друг, %s:
                /start - для того, чтобы бот стартанул
                /help - вызывает данную строку
                /choose_query - задает нужный вам запрос
                /choose_exp - задает нужный вам диапазон опыта
                """.formatted(appUser.getFirstName());
    }

    private String startInteraction(AppUser appUser) {
        return "Добро пожаловать в капитализм, %s!".formatted(appUser.getFirstName());
    }

    private void sendAnswer(String text, Long chatId) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
        answerProducer.produceAnswer(sendMessage);
    }

    private void sendAnswer(String text, Long chatId, ReplyKeyboard replyKeyboard) {
        var sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(replyKeyboard);
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
