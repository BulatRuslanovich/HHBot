package com.bipbup.service;

import com.bipbup.dto.Vacancy;
import com.bipbup.entity.AppUser;

import java.util.List;

public interface APIHandler {
    List<Vacancy> getNewVacancies(AppUser appUser);
}
