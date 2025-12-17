package com.metamapa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. Habilitar CORS para que el Front (puerto 5173) pueda pedir los datos
                .cors().configurationSource(corsConfigurationSource())
                .and()
                // 2. Desactivar CSRF (innecesario para estas APIs REST de lectura)
                .csrf().disable()
                // 3. Permitir acceso público a todos los endpoints de estadísticas
                .authorizeRequests()
                .antMatchers("/api/stats/**").permitAll() // Endpoints de gráficos y CSV
                .anyRequest().permitAll();

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permitir origen del Frontend (React)
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));

        // Métodos permitidos (GET es el más importante para stats)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
