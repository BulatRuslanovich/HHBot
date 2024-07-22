package com.bipbup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class NodeApplication {
    public static void main(String[] args) {
        SpringApplication.run(NodeApplication.class);
    }
}
