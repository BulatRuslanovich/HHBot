package com.bipbup.service.impl;

import com.bipbup.model.Vacancy;
import com.bipbup.service.APIHandler;
import com.bipbup.service.AnswerProducer;
import com.bipbup.service.NotifierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Service
public class NotifierServiceImpl implements NotifierService {
    final private APIHandler apiHandler;
    final private AnswerProducer answerProducer;

    @Override
    public void informAboutNewVacancies(Update update) {
        List<Vacancy> newVacancies = apiHandler.getListWithNewVacancies(LocalDateTime.now().minusHours(3));
        Collections.reverse(newVacancies);

        if (!newVacancies.isEmpty()) {
            for (var newVacancy : newVacancies) {
                createMessageWithVacancy(newVacancy, update);
            }
        }


    }

    private void createMessageWithVacancy(Vacancy newVacancy, Update update) {
        String message = newVacancy.getNameVacancy() + "\n" +
                         newVacancy.getNameEmployer() + "\n" +
                         newVacancy.getNameArea() + "\n" +
                         newVacancy.getPublishedAt().toString() + "\n" +
                         newVacancy.getUrl() + "\n";

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(message);
        sendMessage.setChatId(update.getMessage().getChatId());

        answerProducer.produceAnswer(sendMessage);
    }
}
