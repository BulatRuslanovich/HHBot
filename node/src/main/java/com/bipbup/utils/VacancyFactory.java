package com.bipbup.utils;

import com.bipbup.dto.Vacancy;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public class VacancyFactory {
    private VacancyFactory() {}

    public static Vacancy convertJsonToVacancy(JsonNode vacancy, LocalDateTime publishedAt) {
        return Vacancy.of(
                vacancy.get("id").asText(),
                vacancy.get("name").asText(),
                vacancy.get("employer").get("name").asText(),
                vacancy.get("area").get("name").asText(),
                publishedAt,
                vacancy.get("alternate_url").asText()
        );
    }
}
