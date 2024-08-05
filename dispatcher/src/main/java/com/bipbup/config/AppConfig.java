package com.bipbup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;

@Configuration
public class AppConfig {
    @Bean public StringJsonMessageConverter jsonConverter() {
        return new StringJsonMessageConverter();
    }
}
