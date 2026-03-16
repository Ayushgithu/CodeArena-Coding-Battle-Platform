package com.codearena.backend.config;

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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://localhost:5174",
                "http://localhost:8080",
                "http://127.0.0.1:5500"
        ));
        config.setAllowedHeaders(Arrays.asList(
                "Origin", "Content-Type", "Accept", "Authorization",
                "X-Requested-With", "X-Auth-Token", "X-Requested-With"
        ));
        config.setExposedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Auth-Token"
        ));
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        config.setMaxAge(3600L); // 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}