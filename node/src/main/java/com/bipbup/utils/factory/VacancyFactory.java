package com.bipbup.utils.factory;

import com.bipbup.dto.VacancyDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;

@UtilityClass
public class VacancyFactory {

    public static VacancyDTO createVacancyDTO(JsonNode vacancy,
                                              LocalDateTime publishedAt) {
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
