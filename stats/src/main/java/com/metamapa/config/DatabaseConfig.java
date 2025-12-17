package com.metamapa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de base de datos para el servicio de estadísticas
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.metamapa.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    
    // Configuración de base de datos
    // Se conecta a la misma MySQL del agregador
    // Las entidades se configuran automáticamente por Spring Boot
}
