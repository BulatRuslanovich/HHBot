package com.bipbup.service.impl;

import com.bipbup.dto.VacancyDTO;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.entity.EducationLevelParamEntity;
import com.bipbup.entity.ScheduleParamEntity;
import com.bipbup.enums.impl.EducationLevelParam;
import com.bipbup.enums.impl.ExperienceParam;
import com.bipbup.enums.impl.ScheduleTypeParam;
import com.bipbup.service.APIConnection;
import com.bipbup.service.APIHandler;
import com.bipbup.service.AreaService;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.bipbup.utils.factory.VacancyFactory.createVacancyDTO;

@Slf4j
@RequiredArgsConstructor
@Service
public class APIHandlerImpl implements APIHandler {

    public static final int COUNT_OF_DAYS = 2;

    private static final int COUNT_OF_VACANCIES_IN_PAGE = 100;

    private static final int TIMESTAMP_FULL_LENGTH = 24;

    private static final int TIMESTAMP_TRIMMED_LENGTH = 19;

    private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    private final APIConnection apiConnection;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    private final AreaService areaService;

    @Value("${headhunter.endpoint.searchForVacancy}")
    private String searchForVacancyURI;

    private static boolean isPresentJson(JsonNode jsonNode) {
        return jsonNode != null && !jsonNode.isEmpty();
    }

    private static void addVacanciesFromPage(AppUserConfig config,
                                             List<VacancyDTO> vacancyList,
                                             JsonNode vacanciesOnPage) {
        vacanciesOnPage.forEach(v -> {
            var publishedAt = getPublishedAtFromJson(v);

            if (publishedAt.isBefore(config.getLastNotificationTime()))
                return;

            vacancyList.add(createVacancyDTO(v, publishedAt));
        });
    }

    private static LocalDateTime getPublishedAtFromJson(JsonNode vacancy) {
        var timestamp = vacancy.get("published_at").asText();

        if (timestamp.length() == TIMESTAMP_FULL_LENGTH)
            timestamp = timestamp.substring(0, TIMESTAMP_TRIMMED_LENGTH);

        return LocalDateTime.parse(timestamp,
                DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN));
    }

    @Override
    public List<VacancyDTO> fetchNewVacancies(AppUserConfig config) {
        var request = apiConnection.createRequestWithHeaders();
        var pageCount = fetchPageCount(request, config);
        List<VacancyDTO> vacancyList = new ArrayList<>();

        for (int i = 0; i <= pageCount; i++) {
            processVacancyPage(config, request, i, vacancyList);
        }

        return vacancyList;
    }

    private String encodeQueryText(String queryText) {
        return URLEncoder.encode(queryText, StandardCharsets.UTF_8);
    }

    private JsonNode fetchVacancyPage(HttpEntity<HttpHeaders> request,
                                      int pageNumber,
                                      AppUserConfig config) {
        var vacancySearchUri = generateVacancySearchUri(pageNumber, config);
        var response = restTemplate.exchange(vacancySearchUri, HttpMethod.GET, request, String.class).getBody();

        try {
            return objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return objectMapper.createObjectNode();
        }
    }

    private int fetchPageCount(HttpEntity<HttpHeaders> request,
                               AppUserConfig config) {
        var firstPage = fetchVacancyPage(request, 0, config);

        if (isPresentJson(firstPage)) {
            var totalCount = firstPage.get("found").asInt(0);
            return (int) Math.ceil((double) totalCount / COUNT_OF_VACANCIES_IN_PAGE);
        }

        return 0;
    }

    private void processVacancyPage(AppUserConfig config,
                                    HttpEntity<HttpHeaders> request,
                                    int pageNum,
                                    List<VacancyDTO> vacancyList) {
        var jsonPage = fetchVacancyPage(request, pageNum, config);

        if (isPresentJson(jsonPage))
            addVacanciesFromPage(config, vacancyList, jsonPage.get("items"));
    }

    private void addAreaParam(UriComponentsBuilder builder, AppUserConfig config) {
        Optional.ofNullable(areaService.getAreaIdByName(config.getArea()))
                .ifPresent(areaId -> builder.queryParam("area", areaId));
    }

    private void addExperienceParam(UriComponentsBuilder builder, AppUserConfig config) {
        var experience = config.getExperience();

        if (ExperienceParam.NO_MATTER != experience)
            builder.queryParam("experience", experience.getParam());
    }

    private void addEducationParam(UriComponentsBuilder builder, AppUserConfig config) {
        var levels = Optional.ofNullable(config.getEduParams())
                .stream()
                .flatMap(Collection::stream)
                .map(EducationLevelParamEntity::getParamName)
                .filter(Objects::nonNull)
                .toList();

        if (!levels.isEmpty())
            builder.queryParam("education", levels);
    }

    private void addScheduleParam(UriComponentsBuilder builder, AppUserConfig config) {
        var types = Optional.ofNullable(config.getScheduleParams())
                .stream()
                .flatMap(Collection::stream)
                .map(ScheduleParamEntity::getParamName)
                .filter(Objects::nonNull)
                .toList();

        if (!types.isEmpty())
            builder.queryParam("schedule", types);
    }

    private String generateVacancySearchUri(int pageNumber, AppUserConfig config) {
        var builder = UriComponentsBuilder.fromUriString(searchForVacancyURI)
                .queryParam("page", pageNumber)
                .queryParam("per_page", COUNT_OF_VACANCIES_IN_PAGE)
                .queryParam("text", encodeQueryText(config.getQueryText()))
                .queryParam("search_field", "name")
                .queryParam("period", COUNT_OF_DAYS)
                .queryParam("order_by", "publication_time");

        addAreaParam(builder, config);
        addExperienceParam(builder, config);
        addEducationParam(builder, config);
        addScheduleParam(builder, config);

        return builder.build().toUriString();
    }
}
