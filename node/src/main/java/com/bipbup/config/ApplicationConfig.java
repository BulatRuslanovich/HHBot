package com.bipbup.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.List;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfig {

    private static final int MIN_HASH_LENGTH = 10;

    @Value("${salt}")
    private String salt;

    @Bean
    public Hashids getHashids() {
        return new Hashids(salt, MIN_HASH_LENGTH);
    }

    @Bean
    public StringJsonMessageConverter jsonConverter() {
        return new StringJsonMessageConverter();
    }

    @Bean
    public RestTemplate restTemplate() {
        var objectMapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .build();

        var messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(objectMapper);

        var restTemplate = new RestTemplate();
        restTemplate.setMessageConverters(List.of(messageConverter));

        return restTemplate;
    }
}
