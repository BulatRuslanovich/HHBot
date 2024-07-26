package com.bipbup.service;

import com.bipbup.entity.AppUser;
import com.bipbup.model.Vacancy;

import java.util.List;

public interface APIHandler {
    List<Vacancy> getNewVacancies(AppUser appUser);
}
