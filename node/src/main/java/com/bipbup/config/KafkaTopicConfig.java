package com.bipbup.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final KafkaTopicProperties kafkaTopicProperties;

    @Bean
    public NewTopic answerTopic() {
        return TopicBuilder.name(kafkaTopicProperties.answerTopic()).build();
    }

    @Bean
    public NewTopic textUpdateTopic() {
        return TopicBuilder.name(kafkaTopicProperties.textUpdateTopic()).build();
    }

    @Bean
    public NewTopic callbackQueryUpdateTopic() {
        return TopicBuilder.name(kafkaTopicProperties.callbackQueryUpdateTopic()).build();
    }

    @Bean
    public NewTopic editTopic() {
        return TopicBuilder.name(kafkaTopicProperties.editTopic()).build();
    }
}
