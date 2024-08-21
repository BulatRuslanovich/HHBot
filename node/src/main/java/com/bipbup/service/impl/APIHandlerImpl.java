package com.bipbup.service.impl;

import com.bipbup.dto.VacancyDTO;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.enums.impl.EducationLevelParam;
import com.bipbup.enums.impl.ExperienceParam;
import com.bipbup.enums.impl.ScheduleTypeParam;
import com.bipbup.service.APIConnection;
import com.bipbup.service.APIHandler;
import com.bipbup.utils.AreaUtil;
import com.bipbup.utils.factory.VacancyFactory;
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
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class APIHandlerImpl implements APIHandler {

    @Value("${headhunter.endpoint.searchForVacancy}")
    private String searchForVacancyURI;

    private final APIConnection apiConnection;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    private static final int COUNT_OF_VACANCIES_IN_PAGE = 100;
    public static final int COUNT_OF_DAYS = 4;
    private static final int TIMESTAMP_FULL_LENGTH = 24;
    private static final int TIMESTAMP_TRIMMED_LENGTH = 19;
    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    @Override
    public List<VacancyDTO> getNewVacancies(final AppUserConfig config) {
        var request = apiConnection.createRequestWithHeaders();
        var pageCount = getPageCount(request, config);
        List<VacancyDTO> vacancyDTOList = new ArrayList<>();

        for (int i = 0; i <= pageCount; i++) {
            processVacancyPage(config, request, i, vacancyDTOList);
        }

        return vacancyDTOList;
    }

    private void processVacancyPage(final AppUserConfig appUserConfig,
                                    final HttpEntity<HttpHeaders> request,
                                    final int pageNum,
                                    final List<VacancyDTO> vacancyDTOList) {
        var jsonNode = getVacancyPage(request, pageNum, appUserConfig);

        if (!isEmptyJson(jsonNode)) {
            var vacanciesOnCurrentPage = jsonNode.get("items");
            addVacanciesFromPage(appUserConfig, vacancyDTOList, vacanciesOnCurrentPage);
        }
    }

    private static void addVacanciesFromPage(final AppUserConfig appUserConfig,
                                             final List<VacancyDTO> vacancyDTOList,
                                             final JsonNode vacanciesOnCurrentPage) {
        for (int j = 0; j < vacanciesOnCurrentPage.size(); j++) {
            var vacancy = vacanciesOnCurrentPage.get(j);
            var publishedAt = getPublishedAtFromJson(vacancy);

            if (publishedAt.isBefore(appUserConfig.getLastNotificationTime()))
                return;

            vacancyDTOList.add(VacancyFactory.createVacancyDTO(vacancy, publishedAt));
        }
    }

    private static boolean isEmptyJson(final JsonNode jsonNode) {
        return jsonNode == null || jsonNode.get("items").isEmpty();
    }

    private static LocalDateTime getPublishedAtFromJson(final JsonNode vacancy) {
        var timestamp = vacancy.get("published_at").asText();

        if (timestamp.length() == TIMESTAMP_FULL_LENGTH)
            timestamp = timestamp.substring(0, TIMESTAMP_TRIMMED_LENGTH);

        return LocalDateTime.parse(timestamp,
                DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN));
    }

    private int getPageCount(final HttpEntity<HttpHeaders> request,
                             final AppUserConfig appUserConfig) {
        var firstPage = getVacancyPage(request, 0, appUserConfig);
        var count = firstPage.get("found").asText();
        return Integer.parseInt(count) / COUNT_OF_VACANCIES_IN_PAGE;
    }

    private JsonNode getVacancyPage(final HttpEntity<HttpHeaders> request,
                                    final int pageNumber,
                                    final AppUserConfig appUserConfig) {
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

    private String generateVacancySearchUri(final int pageNumber, final AppUserConfig config) {
        var areaId = AreaUtil.getAreaIdByName(config.getArea());

        var builder = UriComponentsBuilder.fromUriString(searchForVacancyURI)
                .queryParam("page", pageNumber)
                .queryParam("per_page", COUNT_OF_VACANCIES_IN_PAGE)
                .queryParam("text", config.getQueryText().replace("+", "%2B"))
                .queryParam("search_field", "name")
                .queryParam("period", COUNT_OF_DAYS)
                .queryParam("order_by", "publication_time");

        if (areaId != null)
            builder.queryParam("area", Integer.parseInt(areaId));

        if (!config.getExperience().equals(ExperienceParam.NO_MATTER))
            builder.queryParam("experience", config.getExperience().getParam());

        if (config.getEducationLevels() != null && config.getEducationLevels().length != 0) {
            var levels = Arrays.stream(config.getEducationLevels())
                    .map(EducationLevelParam::getParam).toList();
            builder.queryParam("education", levels);
        }

        if (config.getScheduleTypes() != null && config.getScheduleTypes().length != 0) {
            var types = Arrays.stream(config.getScheduleTypes())
                    .map(ScheduleTypeParam::getParam).toList();
            builder.queryParam("schedule", types);
        }

        return builder.build().toUriString();
    }
}
