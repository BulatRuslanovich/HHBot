package com.bipbup.service.impl;

import com.bipbup.dto.Vacancy;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.ExperienceParam;
import com.bipbup.service.APIConnection;
import com.bipbup.service.APIHandler;
import com.bipbup.utils.VacancyFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class APIHandlerImpl implements APIHandler {
    private static final int COUNT_OF_VACANCIES_IN_PAGE = 100;
    public static final int COUNT_OF_DAYS = 4;

    private final APIConnection apiConnection;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${headhunter.endpoint.searchForVacancy}")
    private String searchForVacancyURI;

    @Override
    public List<Vacancy> getNewVacancies(AppUserConfig appUserConfig) {
        var request = apiConnection.createRequestWithHeaders();
        var pageCount = getPageCount(request, appUserConfig);
        List<Vacancy> vacancyList = new ArrayList<>();

        for (int i = 0; i <= pageCount; i++) {
            processVacancyPage(appUserConfig, request, i, vacancyList);
        }

        return vacancyList;
    }

    private void processVacancyPage(AppUserConfig appUserConfig, HttpEntity<HttpHeaders> request, int pageNum, List<Vacancy> vacancyList) {
        var jsonNode = getVacancyPage(request, pageNum, appUserConfig);

        if (!isEmptyJson(jsonNode)) {
            var vacanciesOnCurrentPage = jsonNode.get("items");
            addVacanciesFromPage(appUserConfig, vacancyList, vacanciesOnCurrentPage);
        }
    }

    private static void addVacanciesFromPage(AppUserConfig appUserConfig, List<Vacancy> vacancyList, JsonNode vacanciesOnCurrentPage) {
        for (int j = 0; j < vacanciesOnCurrentPage.size(); j++) {
            var vacancy = vacanciesOnCurrentPage.get(j);
            var publishedAt = getPublishedAtFromJson(vacancy);

            if (publishedAt.isBefore(appUserConfig.getLastNotificationTime()))
                return;

            vacancyList.add(VacancyFactory.convertJsonToVacancy(vacancy, publishedAt));
        }
    }

    private static boolean isEmptyJson(JsonNode jsonNode) {
        return jsonNode == null || jsonNode.get("items").isEmpty();
    }

    private static LocalDateTime getPublishedAtFromJson(JsonNode vacancy) {
        var timestamp = vacancy.get("published_at").asText();
        if (timestamp.length() == 24) timestamp = timestamp.substring(0, 19);
        return LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    }

    private int getPageCount(HttpEntity<HttpHeaders> request, AppUserConfig appUserConfig) {
        var firstPage = getVacancyPage(request, 0, appUserConfig);
        var count = firstPage.get("found").asText();
        return Integer.parseInt(count) / COUNT_OF_VACANCIES_IN_PAGE;
    }

    private JsonNode getVacancyPage(HttpEntity<HttpHeaders> request, int pageNumber, AppUserConfig appUserConfig) {
        var vacancySearchUri = generateVacancySearchUri(pageNumber, appUserConfig);
        var response = restTemplate.exchange(vacancySearchUri, HttpMethod.GET, request, String.class).getBody();
        JsonNode jsonNode = null;

        try {
            jsonNode = objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }

        return jsonNode;
    }

    private String generateVacancySearchUri(int pageNumber, AppUserConfig appUserConfig) {
        var builder = UriComponentsBuilder.fromUriString(searchForVacancyURI)
                .queryParam("page", String.valueOf(pageNumber))
                .queryParam("per_page", COUNT_OF_VACANCIES_IN_PAGE)
                .queryParam("text", appUserConfig.getQueryText())
                .queryParam("search_field", "name")
                .queryParam("area", 88)
                .queryParam("period", COUNT_OF_DAYS)
                .queryParam("order_by", "publication_time");

        if (!appUserConfig.getExperience().equals(ExperienceParam.NO_MATTER))
            builder.queryParam("experience", appUserConfig.getExperience().toString());

        return builder.build().toUriString();

        // TODO: add other searching parameters
    }
}
