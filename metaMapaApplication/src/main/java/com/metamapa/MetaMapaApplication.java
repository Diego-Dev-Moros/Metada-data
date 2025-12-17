package com.metamapa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {
    "com.metamapa.api",           // Controladores del agregador
    "com.metamapa.service",       // Servicios del agregador  
    "com.metamapa.config",        // Configuraciones del agregador
    "com.metamapa.scheduler",     // Schedulers del agregador
    "com.metamapa"                // Resto de componentes
})
@EnableScheduling
public class MetaMapaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MetaMapaApplication.class, args);
    }
} 