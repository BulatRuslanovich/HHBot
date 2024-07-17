package com.bipbup.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    @Value("${spring.kafka.topics.answer-topic}")
    private String answerTopic;
    @Value("${spring.kafka.topics.text-update-topic}")
    private String textUpdateTopic;


    @Bean
    public NewTopic answerTopic() {
        return TopicBuilder.name(answerTopic).build();
    }

    @Bean
    public NewTopic textUpdateTopic() {return TopicBuilder.name(textUpdateTopic).build();}
}
