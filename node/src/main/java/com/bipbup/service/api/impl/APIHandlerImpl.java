package com.bipbup.service.api.impl;

import com.bipbup.dto.VacancyDTO;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.entity.EducationLevel;
import com.bipbup.entity.ScheduleType;
import com.bipbup.enums.impl.EducationLevelParam;
import com.bipbup.enums.impl.ExperienceParam;
import com.bipbup.enums.impl.ScheduleTypeParam;
import com.bipbup.service.api.APIConnection;
import com.bipbup.service.api.APIHandler;
import com.bipbup.service.api.AreaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import static com.bipbup.utils.factory.VacancyFactory.createVacancyDTO;

/**
 * This service handles fetching vacancy data from the API. It uses the user configuration to query for new vacancies
 * and processes the results.
 */
@Slf4j
@Service
@RequiredArgsConstructor
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

	/**
	 * Fetches new vacancies based on user configuration.
	 *
	 * @param config The user configuration containing query parameters.
	 *
	 * @return A list of new vacancies.
	 */
	@Override
	public List<VacancyDTO> fetchNewVacancies(AppUserConfig config) {
		var request = apiConnection.createRequestWithHeaders();
		var pageCount = fetchPageCount(request, config);
		var vacancyList = new ArrayList<VacancyDTO>();

		IntStream.range(0, pageCount)
				.forEach(i -> processVacancyPage(config, request, i, vacancyList));

		return vacancyList;
	}

	private int fetchPageCount(HttpEntity<HttpHeaders> request, AppUserConfig config) {
		var firstPage = fetchVacancyPage(request, 0, config);

		if (firstPage.isEmpty())
			return 0;

		var totalCount = firstPage.get("found").asInt(0);
		return (int) Math.ceil((double) totalCount / COUNT_OF_VACANCIES_IN_PAGE);
	}

	@SneakyThrows
	private JsonNode fetchVacancyPage(HttpEntity<HttpHeaders> request, int pageNumber, AppUserConfig config) {
		var vacancySearchUri = generateVacancySearchUri(pageNumber, config);
		var response = restTemplate.exchange(vacancySearchUri, HttpMethod.GET, request, String.class)
				.getBody();
		return objectMapper.readTree(response);
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

		return builder.build()
				.toUriString();
	}

	private String encodeQueryText(String queryText) {
		return URLEncoder.encode(queryText, StandardCharsets.UTF_8);
	}

	private void addExperienceParam(UriComponentsBuilder builder, AppUserConfig config) {
		var experience = config.getExperience();

		if (ExperienceParam.NO_MATTER != experience)
			builder.queryParam("experience", experience.getParam());
	}

	private void addAreaParam(UriComponentsBuilder builder, AppUserConfig config) {
		Optional.ofNullable(areaService.getAreaIdByName(config.getArea()))
				.ifPresent(areaId -> builder.queryParam("area", areaId));
	}

	public void addEducationParam(UriComponentsBuilder builder, AppUserConfig config) {
		var levels = config.getEducationLevels()
				.stream()
				.map(EducationLevel::getParam)
				.map(EducationLevelParam::getParam)
				.toList();

		if (!levels.isEmpty())
			builder.queryParam("education", levels);
	}

	private void addScheduleParam(UriComponentsBuilder builder, AppUserConfig config) {
		var types = config.getScheduleTypes()
				.stream()
				.map(ScheduleType::getParam)
				.map(ScheduleTypeParam::getParam)
				.toList();

		if (!types.isEmpty())
			builder.queryParam("schedule", types);
	}

	private void processVacancyPage(
			AppUserConfig config, HttpEntity<HttpHeaders> request, int pageNum, List<VacancyDTO> vacancyList
	) {
		var jsonPage = fetchVacancyPage(request, pageNum, config);

		if (!jsonPage.isEmpty())
			addVacanciesFromPage(config, vacancyList, jsonPage.get("items"));
	}

	private void addVacanciesFromPage(AppUserConfig config, List<VacancyDTO> vacancyList, JsonNode vacanciesOnPage) {
		vacanciesOnPage.forEach(vacancy -> {
			var publishedAt = getPublishedAtFromJson(vacancy);

			if (publishedAt.isBefore(config.getLastNotificationTime()))
				return;

			vacancyList.add(createVacancyDTO(vacancy, publishedAt));
		});
	}

	private LocalDateTime getPublishedAtFromJson(JsonNode vacancy) {
		var timestamp = vacancy.get("published_at")
				.asText();

		if (timestamp.length() == TIMESTAMP_FULL_LENGTH)
			timestamp = timestamp.substring(0, TIMESTAMP_TRIMMED_LENGTH);

		return LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN));
	}
}
