package com.bipbup.utils;

import com.bipbup.model.Vacancy;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public class VacancyFactory {
    private VacancyFactory() {}

    public static Vacancy convertJsonToVacancy(JsonNode vacancy, LocalDateTime publishedAt) {
        return Vacancy.builder()
                .headHunterId(vacancy.get("id").asText())
                .nameVacancy(vacancy.get("name").asText())
                .nameEmployer(vacancy.get("employer").get("name").asText())
                .nameArea(vacancy.get("area").get("name").asText())
                .publishedAt(publishedAt)
                .url(vacancy.get("alternate_url").asText())
                .build();
    }
}
