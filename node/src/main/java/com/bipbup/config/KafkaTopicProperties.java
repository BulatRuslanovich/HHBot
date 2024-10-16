package com.bipbup.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "topics", ignoreUnknownFields = false)
public record KafkaTopicProperties (
		String answerTopic,
		String editTopic,
		String textUpdateTopic,
		String callbackQueryUpdateTopic
) {
}
