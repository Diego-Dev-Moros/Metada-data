package com.metamapa.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración de Auth0
 * 
 * Esta clase lee las configuraciones de Auth0 desde application.yml
 * y las hace disponibles como beans de Spring.
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth0")
public class Auth0Properties {
    
    /**
     * Dominio de Auth0 (ej: dev-x8zpgn3i6vnkjg4m.us.auth0.com)
     */
    private String domain;
    
    /**
     * Audience de la API (ej: https://metamapa-api)
     */
    private String audience;
    
    /**
     * Client ID de la aplicación en Auth0
     */
    private String clientId;
    
    /**
     * Client Secret de la aplicación (NO compartir)
     */
    private String clientSecret;
    
    /**
     * Token de la Management API de Auth0 (opcional)
     * Se usa para operaciones administrativas como crear usuarios
     */
    private String managementApiToken;
    
    /**
     * Namespace para claims personalizados (ej: https://metamapa.com)
     */
    private String namespace;
}
