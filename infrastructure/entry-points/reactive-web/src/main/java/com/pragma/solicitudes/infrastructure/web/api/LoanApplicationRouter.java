package com.pragma.solicitudes.infrastructure.web.api;


import com.pragma.solicitudes.infrastructure.web.api.LoanApplicationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class LoanApplicationRouter {

    @Bean
    public RouterFunction<ServerResponse> loanApplicationRoutes(LoanApplicationHandler handler) {
        return route(POST("/api/v1/solicitud"), handler::createLoanApplication);
    }
}