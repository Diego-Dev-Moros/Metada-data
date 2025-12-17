package com.metamapa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class FuenteDinamicaApplication {
    public static void main(String[] args) {
        SpringApplication.run(FuenteDinamicaApplication.class, args);
    }
}
