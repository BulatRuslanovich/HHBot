package com.bipbup.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "keyboard")
public class KeyboardProperties {
    private String noExperience;
    private String oneToThreeYears;
    private String threeToSixYears;
    private String moreThanSixYears;
    private String noFilter;
}
