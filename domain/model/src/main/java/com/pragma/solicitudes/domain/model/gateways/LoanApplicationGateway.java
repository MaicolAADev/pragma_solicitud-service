package com.pragma.solicitudes.domain.model.gateways;


import com.pragma.solicitudes.domain.model.LoanApplication;
import reactor.core.publisher.Mono;

public interface LoanApplicationGateway {
    Mono<LoanApplication> save(LoanApplication loanApplication);
    Mono<Boolean> existsByClientDocument(String clientDocument);
}


