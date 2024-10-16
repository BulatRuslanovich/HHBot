package com.bipbup.service.api;

import com.bipbup.dto.Vacancy;
import com.bipbup.entity.AppUserConfig;

import java.util.List;

public interface VacancyService {

    List<Vacancy> fetchNewVacancies(AppUserConfig config);
}
