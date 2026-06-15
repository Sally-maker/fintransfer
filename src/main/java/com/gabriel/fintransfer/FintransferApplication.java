package com.gabriel.fintransfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FintransferApplication {

    public static void main(String[] args) {
        SpringApplication.run(FintransferApplication.class, args);
    }
}
