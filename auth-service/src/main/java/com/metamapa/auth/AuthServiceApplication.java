package com.metamapa.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Aplicación principal del servicio de autenticación y autorización
 * 
 * Este módulo proporciona:
 * - Validación de JWT tokens de Auth0
 * - Control de acceso basado en roles (RBAC)
 * - Endpoints de información de usuario
 * - Integración con Auth0 Management API
 * 
 * @author MetaMapa Team
 * @version 1.0
 */
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
