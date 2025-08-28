package com.pragma.solicitudes.infrastructure.web.r2dbc.persistence.adapter;

import com.pragma.solicitudes.domain.model.LoanApplication;
import com.pragma.solicitudes.domain.model.gateways.LoanApplicationGateway;
import com.pragma.solicitudes.infrastructure.web.r2dbc.persistence.entity.LoanApplicationEntity;
import com.pragma.solicitudes.infrastructure.web.r2dbc.persistence.mapper.LoanApplicationMapper;
import com.pragma.solicitudes.infrastructure.web.r2dbc.persistence.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class LoanApplicationGatewayAdapter implements LoanApplicationGateway {

    private final LoanApplicationRepository repository;
    private final LoanApplicationMapper mapper;

    @Override
    public Mono<LoanApplication> save(LoanApplication loanApplication) {
        LoanApplicationEntity entity = mapper.toEntity(loanApplication);
        return repository.save(entity)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByClientDocument(String clientDocument) {
        return repository.existsByClientDocument(clientDocument);
    }
}
