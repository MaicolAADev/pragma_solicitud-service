package com.pragma.solicitudes.infrastructure.web.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Loan Application Service API",
        version = "1.0",
        description = "API para gestión de solicitudes de préstamo"
))
public class SwaggerConfig {
}