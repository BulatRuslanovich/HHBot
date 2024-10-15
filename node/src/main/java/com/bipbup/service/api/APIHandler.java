package com.bipbup.service.api;

import com.bipbup.dto.VacancyDTO;
import com.bipbup.entity.AppUserConfig;

import java.util.List;

public interface APIHandler {

    List<VacancyDTO> fetchNewVacancies(AppUserConfig config);
}
