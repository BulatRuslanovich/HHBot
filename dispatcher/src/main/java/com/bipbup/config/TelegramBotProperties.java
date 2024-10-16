package com.bipbup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot", ignoreUnknownFields = false)
public record TelegramBotProperties(
		String url,
		String username,
		String token
) {}
