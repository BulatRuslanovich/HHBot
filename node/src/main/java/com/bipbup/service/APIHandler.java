package com.bipbup.service;

import com.bipbup.dto.VacancyDTO;
import com.bipbup.entity.AppUserConfig;

import java.util.List;

public interface APIHandler {

    List<VacancyDTO> getNewVacancies(final AppUserConfig config);
}
