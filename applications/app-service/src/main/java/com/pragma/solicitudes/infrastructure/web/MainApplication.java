package com.pragma.solicitudes.infrastructure.web;

import com.pragma.solicitudes.infrastructure.web.r2dbc.config.PostgresqlConnectionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan("com.pragma.solicitudes.infrastructure.web.r2dbc.config")
@EnableConfigurationProperties(PostgresqlConnectionProperties.class)
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}