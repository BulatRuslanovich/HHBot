package com.bipbup.service;

import com.bipbup.model.Vacancy;

import java.time.LocalDateTime;
import java.util.List;

public interface APIHandler {
    List<Vacancy> getListWithNewVacancies(LocalDateTime dateOfPublicationOfLastVacancy);
}
