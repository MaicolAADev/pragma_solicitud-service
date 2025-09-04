package com.crediya.r2dbc.loantype;

import com.crediya.model.loantype.LoanType;
import com.crediya.model.loantype.gateways.LoanTypeRepository;
import com.crediya.r2dbc.entity.LoanTypeEntity;
import com.crediya.r2dbc.helper.ReactiveAdapterOperations;
import lombok.extern.slf4j.Slf4j;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Repository
@Slf4j
public class LoanTypeReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        LoanType,
        LoanTypeEntity,
        String,
        LoanTypeReactiveRepository
        > implements LoanTypeRepository {
    public LoanTypeReactiveRepositoryAdapter(LoanTypeReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, LoanType.class));
    }

    @Override
    public Mono<LoanType> getLoanTypeByName(String name) {
        return this.repository.findByName(name)
                .filter(Objects::nonNull)
                .map(entity -> mapper.map(entity, LoanType.class))
                .onErrorMap(e -> {
                    log.error("Error al consultar tipo de préstamo por nombre: {}", e.getMessage());
                    return new RuntimeException("Error al consultar tipo de préstamo por nombre", e);
                });
    }

    @Override
    public Mono<LoanType> getLoanTypeById(Integer id) {
        return this.repository.findById(String.valueOf(id))
                .filter(Objects::nonNull)
                .map(entity -> mapper.map(entity, LoanType.class))
                .onErrorMap(e -> {
                    log.error("Error al consultar tipo de préstamo por ID: {}", e.getMessage());
                    return new RuntimeException("Error al consultar tipo de préstamo por ID", e);
                });
    }
}
