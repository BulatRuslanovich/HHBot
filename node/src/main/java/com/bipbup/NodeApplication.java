package com.bipbup;

import com.bipbup.config.HeadHunterProperties;
import com.bipbup.config.KafkaTopicProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableCaching
@EnableScheduling
@EnableJpaRepositories("com.bipbup")
@EntityScan("com.bipbup")
@EnableConfigurationProperties({KafkaTopicProperties.class, HeadHunterProperties.class})
@SpringBootApplication
public class NodeApplication {
    public static void main(String[] args) {
        SpringApplication.run(NodeApplication.class, args);
    }
}
