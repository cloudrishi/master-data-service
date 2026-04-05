package com.rish.masterdata;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class MasterDataServiceApplication {

    private static final Logger log =
            LoggerFactory.getLogger(MasterDataServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(
                MasterDataServiceApplication.class, args);
    }

    @Bean
    public CommandLineRunner runner(
            @Value("${spring.security.oauth2.client.registration.github.client-id}")
            String clientId) {
        return args -> log.info("GitHub Client ID loaded: {}", clientId);
    }
}