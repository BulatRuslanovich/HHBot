package com.bipbup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "headhunter", ignoreUnknownFields = false)
public record HeadHunterProperties (
		int countVacanciesInPage,
		int periodOfDays,
		String vacanciesGetUrl,
		String areaGetUrl
) {}
