package com.bipbup.utils.factory;

import com.bipbup.dto.VacancyDTO;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public class VacancyFactory {
    private VacancyFactory() {
    }

    public static VacancyDTO createVacancyDTO(final JsonNode vacancy,
                                              final LocalDateTime publishedAt) {
        return VacancyDTO.of(
                vacancy.get("id").asText(),
                vacancy.get("name").asText(),
                vacancy.get("employer").get("name").asText(),
                vacancy.get("area").get("name").asText(),
                publishedAt,
                vacancy.get("alternate_url").asText()
        );
    }
}
