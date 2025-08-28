package com.pragma.solicitudes.infrastructure.web.r2dbc.persistence.repository;


import com.pragma.solicitudes.infrastructure.web.r2dbc.persistence.entity.LoanApplicationEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface LoanApplicationRepository extends R2dbcRepository<LoanApplicationEntity, Long> {
    Mono<Boolean> existsByClientDocument(String clientDocument);
}