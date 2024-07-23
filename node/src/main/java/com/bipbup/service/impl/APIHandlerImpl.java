package com.bipbup.service.impl;

import com.bipbup.entity.AppUser;
import com.bipbup.enums.ExperienceParam;
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
    public List<Vacancy> getListWithNewVacancies(AppUser appUser) {
        var request = apiConnection.createRequestWithHeaders();
        List<Vacancy> vacancies = new ArrayList<>();
        var numberOfPages = getNumberOfPages(request, appUser);

        for (int i = 0; i <= numberOfPages; i++) {
            var jsonNode = getPageWithVacancies(request, i, appUser);
            var jsonNodeContainsVacancies = jsonNode != null && !jsonNode.get("items").isEmpty();

            if (jsonNodeContainsVacancies) {
                JsonNode vacanciesOnCurrentPage = jsonNode.get("items");
                for (int j = 0; j < vacanciesOnCurrentPage.size(); j++) {
                    var vacancy = vacanciesOnCurrentPage.get(j);
                    var timestamp = vacancy.get("published_at").asText();

                    if (timestamp.length() == 24) {
                        timestamp = timestamp.substring(0, 19);
                    }

                    var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    var publishedAt = LocalDateTime.parse(timestamp, formatter);

                    var currentVacancyIsNotNew = publishedAt.isBefore(appUser.getLastNotificationTime());

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

    private int getNumberOfPages(HttpEntity<HttpHeaders> request, AppUser appUser) {
        var firstPageWithVacancies = getPageWithVacancies(request, 0, appUser);
        var numberOfVacancies = firstPageWithVacancies.get("found").asText();
        return Integer.parseInt(numberOfVacancies) / 100;
    }

    private JsonNode getPageWithVacancies(HttpEntity<HttpHeaders> request, int pageNumber, AppUser appUser) {
        var uri = getUri(pageNumber, appUser);

        var response = restTemplate.exchange(uri, HttpMethod.GET, request, String.class).getBody();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            log.error(e);
        }

        return jsonNode;
    }

    private String getUri(int pageNumber, AppUser appUser) {
        var builder = UriComponentsBuilder
                .fromUriString(searchForVacancyURI)
                .queryParam("page", String.valueOf(pageNumber))
                .queryParam("per_page", "100")
                .queryParam("text", appUser.getQueryText())
                .queryParam("search_field", "name")
                .queryParam("area", 88) // id Казани 88
                .queryParam("period", "1") // за последний день (1)
                .queryParam("order_by", "publication_time");

        if (!appUser.getExperience().equals(ExperienceParam.NO_MATTER))
            builder.queryParam("experience", appUser.getExperience().toString());

        return builder.build().toUriString();
    }
}
