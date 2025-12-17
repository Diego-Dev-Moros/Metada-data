package com.metamapa.config;

import com.metamapa.security.Auth0JwtGrantedAuthoritiesConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/**
 * Configuración de seguridad para el módulo Gestor de Solicitudes
 * 
 * Incluye:
 * - Validación de JWT de Auth0
 * - Autorización basada en roles (USER, CONTRIBUTOR, ADMIN)
 * - CORS para frontend
 * - Habilitación de @PreAuthorize en controladores
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final Auth0JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable()
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeRequests()
                // APIs públicas - sin autenticación
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/actuator/health", "/actuator/info").permitAll()
                .antMatchers("/error").permitAll()
                
                // API Administrativa - solo ADMIN
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                
                // API Interna - usuarios autenticados (USER, CONTRIBUTOR, ADMIN)
                // Las restricciones granulares se manejan con @PreAuthorize en los controladores
                .antMatchers("/api/interna/**").authenticated()
                
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
                .and()
                // Configurar OAuth2 Resource Server con JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                    )
                );

        return http.build();
    }
    
    /**
     * Usa nuestro converter personalizado que busca roles en múltiples ubicaciones
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        
        return jwtAuthenticationConverter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:5173")); // Frontend React
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight por 1 hora

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}