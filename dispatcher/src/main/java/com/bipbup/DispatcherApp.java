package com.bipbup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DispatcherApp {
    public static void main(String[] args) {
        SpringApplication.run(DispatcherApp.class);
    }

//    @Bean
//    CommandLineRunner commandLineRunner(KafkaTemplate<String, String> template) {
//        return args -> {
//            for (int i = 0; i < 10; i++) {
//                template.send("textTopic", String.valueOf(i));
//            }
//            template.send("textTopic", "test");
//        };
//    }
}
