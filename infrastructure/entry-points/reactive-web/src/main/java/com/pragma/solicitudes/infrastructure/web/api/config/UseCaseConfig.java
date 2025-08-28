package com.pragma.solicitudes.infrastructure.web.api.config;

import com.pragma.solicitudes.domain.usecase.LoanApplicationUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.pragma.solicitudes.domain.model.gateways.LoanApplicationGateway;

@Configuration
public class UseCaseConfig {

    @Bean
    public LoanApplicationUseCase loanApplicationUseCase(LoanApplicationGateway loanApplicationGateway) {
        return new LoanApplicationUseCase(loanApplicationGateway);
    }
}
