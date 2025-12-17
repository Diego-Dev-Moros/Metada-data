package com.metamapa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = {"com.metamapa.entities"})  // Escanear entidades del módulo domain
@EnableJpaRepositories(basePackages = {"com.metamapa.repository"})  // Escanear repositorios
public class GestorSolicitudesApplication {
    public static void main(String[] args) { SpringApplication.run(GestorSolicitudesApplication.class, args); }
}
