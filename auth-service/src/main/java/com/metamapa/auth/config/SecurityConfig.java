package com.metamapa.auth.config;

import com.metamapa.auth.converter.Auth0JwtAuthenticationConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración de seguridad para MetaMapa
 * 
 * Esta clase configura:
 * 1. Validación de JWT tokens de Auth0
 * 2. Reglas de autorización para endpoints
 * 3. CORS para permitir requests desde frontend
 * 4. Conversión de roles de Auth0 a Spring Security
 * 
 * El flujo de seguridad es:
 * Request → CORS Filter → JWT Validation → Role Check → Controller
 * 
 * @author MetaMapa Team
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize en controladores
@RequiredArgsConstructor
public class SecurityConfig {

    private final Auth0JwtAuthenticationConverter jwtAuthenticationConverter;
    private final CorsProperties corsProperties;

    /**
     * Configura el filtro de seguridad principal
     * 
     * Define qué endpoints requieren autenticación y qué roles
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF (no necesario para APIs stateless con JWT)
            .csrf(csrf -> csrf.disable())
            
            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configurar gestión de sesiones (stateless - sin sesiones en servidor)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configurar reglas de autorización
            .authorizeHttpRequests(auth -> auth
                // ========== ENDPOINTS PÚBLICOS (sin autenticación) ==========
                .antMatchers("/api/publica/**").permitAll()
                .antMatchers("/actuator/health", "/actuator/info").permitAll()
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .antMatchers("/error").permitAll()
                
                // ========== ENDPOINTS ADMINISTRATIVOS (solo ADMIN) ==========
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                
                // ========== ENDPOINTS INTERNOS (cualquier usuario autenticado) ==========
                .antMatchers("/api/interna/**").authenticated()
                
                // ========== ENDPOINTS DE DEBUG (solo en desarrollo) ==========
                .antMatchers("/api/debug/**").permitAll()
                
                // ========== CUALQUIER OTRO REQUEST ==========
                .anyRequest().authenticated()
            )
            
            // Configurar OAuth2 Resource Server con JWT
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    // Usar nuestro converter personalizado para extraer roles
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                )
            );

        return http.build();
    }

    /**
     * Configuración CORS para permitir requests desde el frontend
     * 
     * CORS (Cross-Origin Resource Sharing) permite que el frontend
     * (ej: http://localhost:5173) haga peticiones al backend
     * (ej: http://localhost:8080) que están en dominios diferentes
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Orígenes permitidos (frontend)
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        
        // Métodos HTTP permitidos
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        
        // Headers permitidos
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        
        // Permitir credenciales (cookies, authorization headers)
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        
        // Tiempo de cache para preflight requests
        configuration.setMaxAge(corsProperties.getMaxAge());

        // Aplicar configuración a todos los endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
