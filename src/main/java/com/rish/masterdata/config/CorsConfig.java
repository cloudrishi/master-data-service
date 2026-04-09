package com.rish.masterdata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();

        // Allowed origins - Next.js dev server
        config.setAllowedOrigins(List.of(
                "http://localhost:3000" // Next.js
        ));

        // Allowed HTTP methods
        config.setAllowedMethods((Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        )));

        // Allowed headers
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Cookie"
        ));

        // Expose header to browser and Set-Cookies
        config.setExposedHeaders(
                Arrays.asList("Authorization",
                        "Set-Cookie"
                )

        );

        // Allow cookies for OAuth2 state
        config.setAllowCredentials(true);

        // Cache preflight for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
