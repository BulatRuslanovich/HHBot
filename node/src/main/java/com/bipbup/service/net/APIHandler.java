package com.bipbup.service.net;

import com.bipbup.dto.VacancyDTO;
import com.bipbup.entity.AppUserConfig;

import java.util.List;

public interface APIHandler {

    List<VacancyDTO> fetchNewVacancies(AppUserConfig config);
}
