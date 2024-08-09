package com.bipbup.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Value("${salt}")
    private String salt;
    @Bean
    public Hashids getHashids() {
        var minHashLength = 10;
        return new Hashids(salt, minHashLength);
    }

    @Bean
    public StringJsonMessageConverter jsonConverter() {
        return new StringJsonMessageConverter();
    }
    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder builder) {
        return builder.build();
    }
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
