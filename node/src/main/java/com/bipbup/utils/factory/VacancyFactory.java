package com.bipbup.utils.factory;

import com.bipbup.dto.VacancyDTO;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import lombok.experimental.UtilityClass;

@UtilityClass
public class VacancyFactory {

    @SuppressWarnings("checkstyle:MultipleStringLiterals")
    public static VacancyDTO createVacancyDTO(JsonNode vacancy,
                                              LocalDateTime publishedAt) {
        return VacancyDTO.of(
                vacancy.get("id").asText(),
                vacancy.get("name").asText(),
                vacancy.get("employer").get("name").asText(),
                vacancy.get("area").get("name").asText(),
                publishedAt,
                vacancy.get("alternate_url").asText(),
                vacancy.get("professional_roles").iterator().next().get("name").asText(),
                vacancy.get("schedule").get("name").asText(),
                vacancy.get("experience").get("name").asText(),
                vacancy.get("employment").get("name").asText()
        );
    }
}
