package com.movigestion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Permitir el envío de credenciales/tokens
        config.setAllowCredentials(true);
        // Agregar tanto el puerto 4200 como el 5173 por si decides volver a la config por defecto de Vite
        config.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:5173"));
        // Permitir las cabeceras críticas para JWT y JSON
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization"));
        // Permitir todos los verbos HTTP incluyendo OPTIONS (Preflight)
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}