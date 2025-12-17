package com.metamapa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.metamapa.repository"})
@EntityScan(basePackages = {"com.metamapa.entities"})
public class FuenteEstaticaApplication {
    public static void main(String[] args) {
        SpringApplication.run(FuenteEstaticaApplication.class, args);
    }
} 