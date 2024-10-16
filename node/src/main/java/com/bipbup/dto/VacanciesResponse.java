package com.bipbup.dto;

import java.util.List;

public record VacanciesResponse(
		List<Vacancy> items,
		int found
) {}



