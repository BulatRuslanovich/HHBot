package com.bipbup.service.api.impl;

import com.bipbup.config.HeadHunterProperties;
import com.bipbup.dto.VacanciesResponse;
import com.bipbup.dto.Vacancy;
import com.bipbup.entity.AppUserConfig;
import com.bipbup.entity.EducationLevel;
import com.bipbup.entity.ScheduleType;
import com.bipbup.enums.impl.EducationLevelParam;
import com.bipbup.enums.impl.ExperienceParam;
import com.bipbup.enums.impl.ScheduleTypeParam;
import com.bipbup.service.api.VacancyService;
import com.bipbup.service.cache.AreaCacheService;
import com.bipbup.utils.DateTimeUtil;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * This service handles fetching vacancy data from the API. It uses the user configuration to query for new vacancies
 * and processes the results.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VacancyServiceImpl implements VacancyService {

	private final RestTemplate restTemplate;

	private final AreaCacheService areaCacheService;

	private final HeadHunterProperties headHunterProperties;

	private List<Vacancy> vacancyList;

	private AppUserConfig config;

	/**
	 * Fetches new vacancies based on user configuration.
	 *
	 * @param config The user configuration containing query parameters.
	 *
	 * @return A list of new vacancies.
	 */
	@Override
	public List<Vacancy> fetchNewVacancies(AppUserConfig config) {
		// TODO: будет ли работать на синглтоне бина
		this.config = config;
		var pageCount = fetchPageCount();
		vacancyList = new ArrayList<>();

		for (int numPage = 0; numPage < pageCount; ++numPage)
			processVacancyPage(numPage);

		return vacancyList;
	}

	private int fetchPageCount() {
		var optionalPage = fetchVacancyPage(0);

		if (optionalPage.isEmpty()
		    || optionalPage.get().items() == null
		    || optionalPage.get().items().isEmpty())
			return 0;

		var totalCount = optionalPage.get().found();
		return (int) Math.ceil((double) totalCount / headHunterProperties.countVacanciesInPage());
	}

	private Optional<VacanciesResponse> fetchVacancyPage(int pageNumber) {
		var vacanciesGetUri = createVacanciesGetUri(pageNumber);
		return Optional.ofNullable(restTemplate.getForObject(vacanciesGetUri, VacanciesResponse.class));
	}

	private String createVacanciesGetUri(int pageNumber) {
		var builder = UriComponentsBuilder.fromUriString(headHunterProperties.vacanciesGetUrl())
				.queryParam("page", pageNumber)
				.queryParam("per_page", headHunterProperties.countVacanciesInPage())
				.queryParam("text", encodeQueryText(config.getQueryText()))
				.queryParam("search_field", "name")
				.queryParam("period", headHunterProperties.periodOfDays())
				.queryParam("order_by", "publication_time");

		addArea(builder);
		addExperience(builder);
		addEducation(builder);
		addSchedule(builder);

		return builder.build().toString();
	}

	private String encodeQueryText(String queryText) {
		return URLEncoder.encode(queryText, StandardCharsets.UTF_8);
	}

	private void addExperience(UriComponentsBuilder builder) {
		var experience = config.getExperience();

		if (ExperienceParam.NO_MATTER != experience)
			builder.queryParam("experience", experience.getParam());
	}

	private void addArea(UriComponentsBuilder builder) {
		Optional.ofNullable(areaCacheService.getAreaIdByName(config.getArea()))
				.ifPresent(areaId -> builder.queryParam("area", areaId));
	}

	public void addEducation(UriComponentsBuilder builder) {
		var levels = config.getEducationLevels()
				.stream()
				.map(EducationLevel::getParam)
				.map(EducationLevelParam::getParam)
				.toList();

		if (!levels.isEmpty())
			builder.queryParam("education", levels);
	}

	private void addSchedule(UriComponentsBuilder builder) {
		var types = config.getScheduleTypes()
				.stream()
				.map(ScheduleType::getParam)
				.map(ScheduleTypeParam::getParam)
				.toList();

		if (!types.isEmpty())
			builder.queryParam("schedule", types);
	}

	private void processVacancyPage(int pageNum) {
		var optionalPage = fetchVacancyPage(pageNum);

		if (optionalPage.isPresent()
		    && optionalPage.get().items() != null
		    && !optionalPage.get().items().isEmpty())
			fillListFromPage(optionalPage.get().items());
	}

	private void fillListFromPage(List<Vacancy> vacancies) {
		for (var vacancy : vacancies) {
			var publishedAt = DateTimeUtil.convertToDate(vacancy.publishedAt());

			if (publishedAt.isBefore(config.getLastNotificationTime()))
				return;

			vacancyList.add(vacancy);
		}
	}
}
