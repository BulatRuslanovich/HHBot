package com.bipbup.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

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
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }
}
