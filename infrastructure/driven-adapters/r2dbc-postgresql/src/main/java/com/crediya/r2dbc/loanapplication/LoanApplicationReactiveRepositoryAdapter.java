package com.crediya.r2dbc.loanapplication;

import com.crediya.model.loanapplication.LoanApplication;
import com.crediya.model.loanapplication.LoanApplicationWithExtras;
import com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import com.crediya.r2dbc.entity.LoanApplicationEntity;
import com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import io.micrometer.common.lang.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
public class LoanApplicationReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        LoanApplication,
        LoanApplicationEntity,
        String,
        LoanApplicationReactiveRepository
        > implements LoanApplicationRepository {

    private final TransactionalOperator transactionalOperator;

    public LoanApplicationReactiveRepositoryAdapter(LoanApplicationReactiveRepository repository, ObjectMapper mapper, TransactionalOperator transactionalOperator) {
        super(repository, mapper, d -> mapper.map(d, LoanApplication.class));
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<LoanApplication> save(LoanApplication loanApplication, Integer stateId, Integer loanTypeId) {
        LoanApplicationEntity entity = mapper.map(loanApplication, LoanApplicationEntity.class);
        entity.setStateId(stateId);
        entity.setLoanTypeId(loanTypeId);
        return this.repository.save(entity)
                .map(saved -> {
                    LoanApplication result = mapper.map(saved, LoanApplication.class);
                    result.setState(loanApplication.getState());
                    result.setLoanType(loanApplication.getLoanType());
                    return result;
                })
                .onErrorMap(e -> new RuntimeException("Error al guardar la solicitud", e))
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<LoanApplicationWithExtras> findByStateIds(int page,
                                                          int size,
                                                          @Nullable String email,
                                                          @Nullable String loanType,
                                                          @Nullable String status) {
        int offset = page * size;
        log.info("Ejecutando query con offset={} size={}", offset, size);

        return repository.findAllFiltered(offset, size, email, loanType, status)
                .doOnSubscribe(sub -> log.info("Consultando solicitudes en BD..."))
                .doOnNext(entity -> log.info("Entidad cruda desde BD: {}", entity))
                .map(entity -> mapper.map(entity, LoanApplicationWithExtras.class))
                .doOnNext(mapped -> log.info("Entidad mapeada: {}", mapped))
                .onErrorMap(e -> {
                    log.error("Error al obtener las solicitudes: {}", e.getMessage(), e);
                    return new RuntimeException("Error al obtener las solicitudes", e);
                });
    }


    @Override
    public Flux<LoanApplicationWithExtras> findApprovedByIdentity(String identityDocument) {
        return repository.findApprovedByIdentity(identityDocument)
                .map(entity -> mapper.map(entity, LoanApplicationWithExtras.class))
                .onErrorMap(e -> new RuntimeException("Error al obtener solicitudes aprobadas", e));
    }

}
