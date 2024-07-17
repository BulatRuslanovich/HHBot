package com.bipbup.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface NotifierService {
    void informAboutNewVacancies(Update update);
}
