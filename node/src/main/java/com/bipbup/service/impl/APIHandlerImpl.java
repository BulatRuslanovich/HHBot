package com.bipbup.service.impl;

import com.bipbup.model.Vacancy;
import com.bipbup.service.APIConnection;
import com.bipbup.service.APIHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Log4j
@RequiredArgsConstructor
@Service
public class APIHandlerImpl implements APIHandler {
    private final APIConnection apiConnection;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    @Value("${headhunter.endpoint.searchForVacancy}")
    private String searchForVacancyURI;


    @Override
    public List<Vacancy> getListWithNewVacancies(LocalDateTime dateOfPublicationOfLastVacancy) {
        var request = apiConnection.createRequestWithHeaders();
        List<Vacancy> vacancies = new ArrayList<>();
        int numberOfPages = getNumberOfPages(request);

        for (int i = 0; i <= numberOfPages; i++) {
            JsonNode jsonNode = getPageWithVacancies(request, i);
            boolean jsonNodeContainsVacancies = jsonNode != null && !jsonNode.get("items").isEmpty();

            if (jsonNodeContainsVacancies) {
                JsonNode vacanciesOnCurrentPage = jsonNode.get("items");
                for (int j = 0; j < vacanciesOnCurrentPage.size(); j++) {
                    JsonNode vacancy = vacanciesOnCurrentPage.get(j);

                    String timestamp = vacancy.get("published_at").asText();
                    // Remove timezone part
                    if (timestamp.length() == 24) { // Handles the case where timezone is of the form +0300 or -0300
                        timestamp = timestamp.substring(0, 19);
                    }
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    LocalDateTime publishedAt = LocalDateTime.parse(timestamp, formatter);

                    boolean currentVacancyIsNotNew = publishedAt.isBefore(dateOfPublicationOfLastVacancy);
                    if (currentVacancyIsNotNew) {
                        return vacancies;
                    }
                    vacancies.add(convertIntoVacancy(vacancy, publishedAt));
                }
            }
        }

        return vacancies;
    }


    private Vacancy convertIntoVacancy(JsonNode vacancy, LocalDateTime publishedAt) {
        return Vacancy.builder()
                .headHunterId(vacancy.get("id").asText())
                .nameVacancy(vacancy.get("name").asText())
                .nameEmployer(vacancy.get("employer").get("name").asText())
                .nameArea(vacancy.get("area").get("name").asText())
                .publishedAt(publishedAt)
                .url(vacancy.get("alternate_url").asText())
                .build();
    }

    private int getNumberOfPages(HttpEntity<HttpHeaders> request) {
        JsonNode firstPageWithVacancies = getPageWithVacancies(request, 0);
        String numberOfVacancies = firstPageWithVacancies.get("found").asText();
        return Integer.parseInt(numberOfVacancies) / 100;
    }

    private JsonNode getPageWithVacancies(HttpEntity<HttpHeaders> request, int pageNumber) {
        String uri = getUri(pageNumber);

        var response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class).getBody();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            log.error(e);
        }

        return jsonNode;
    }

    private String getUri(int pageNumber) {
        String uri = searchForVacancyURI;
        return UriComponentsBuilder
                .fromUriString(uri)
                .queryParam("page", String.valueOf(pageNumber))
                .queryParam("per_page", "100")
                .queryParam("text", "Java")
                .queryParam("search_field", "name")
                .queryParam("area", 88) //id Казани 88
                .queryParam("period", "1")
                .queryParam("order_by", "publication_time")
                .build().toUriString();
    }
}
