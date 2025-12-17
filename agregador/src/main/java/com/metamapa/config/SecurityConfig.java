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
                // 1. Habilitar CORS explícitamente antes de cualquier otra cosa
                .cors().configurationSource(corsConfigurationSource())
                .and()
                // 2. Deshabilitar CSRF (común en APIs REST stateless)
                .csrf().disable()
                .authorizeRequests()
                // Permitir acceso público a tus endpoints de API
                .antMatchers("/api/**").permitAll()
                // Permitir acceso a la consola H2 (si la usas) y Swagger
                .antMatchers("/h2-console/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                .anyRequest().permitAll();

        return http.build();
    }

    /**
     * Definición Global de CORS para Spring Security
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permitir el origen de tu Frontend (React suele ser puerto 5173 o 3000)
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:5173"));
        // O usa "*" para desarrollo rápido (menos seguro):
        // configuration.setAllowedOriginPatterns(Collections.singletonList("*"));

        // Métodos permitidos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Cabeceras permitidas
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));

        // Permitir credenciales (cookies, headers de auth)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}