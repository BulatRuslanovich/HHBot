//package com.bipbup.config;
//
//import org.apache.kafka.clients.admin.NewTopic;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.config.TopicBuilder;
//
//@Configuration
//public class KafkaTopicConfig {
//    @Value("${kafka.text-topic-name}")
//    private String textTopicName;
//
//
//    @Bean
//    public NewTopic textTopic() {
//        return TopicBuilder.name(textTopicName).build();
//    }
//}
