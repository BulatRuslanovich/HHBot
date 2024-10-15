package com.bipbup.utils.factory;

import com.bipbup.dto.VacancyDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VacancyFactoryTest {

    @Test
    @DisplayName("Should create VacancyDTO from JsonNode")
    void testCreateVacancyDTO_ValidJsonNode() throws Exception {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = """
                {
                    "id": "123",
                    "name": "Software Engineer",
                    "employer": { "name": "Tech Corp" },
                    "area": { "name": "Remote" },
                    "alternate_url": "http://example.com",
                    "professional_roles": [{ "name": "develop" }],
                    "schedule": { "name": "Remote" },
                    "experience": { "name": "Remote" },
                    "employment": { "name": "Remote" }
                }
                """;

        JsonNode vacancyNode = objectMapper.readTree(jsonString);
        LocalDateTime publishedAt = LocalDateTime.now();

        // Act
        VacancyDTO vacancyDTO = VacancyFactory.createVacancyDTO(vacancyNode, publishedAt);

        // Assert
        assertNotNull(vacancyDTO);
        assertEquals("Software Engineer", vacancyDTO.getNameVacancy());
        assertEquals("Tech Corp", vacancyDTO.getNameEmployer());
        assertEquals("Remote", vacancyDTO.getNameArea());
        assertEquals(publishedAt, vacancyDTO.getPublishedAt());
        assertEquals("http://example.com", vacancyDTO.getUrl());
    }
}