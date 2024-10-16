package com.bipbup.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "topics")
public class KafkaTopicProperties {

	private String answerTopic;

	private String editTopic;

	private String textUpdateTopic;

	private String callbackQueryUpdateTopic;
}
