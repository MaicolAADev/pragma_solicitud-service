package com.crediya.r2dbc.loantype;

import com.crediya.r2dbc.entity.LoanTypeEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LoanTypeReactiveRepository extends ReactiveCrudRepository<LoanTypeEntity, String>, ReactiveQueryByExampleExecutor<LoanTypeEntity>{
    Mono<LoanTypeEntity> findByName(String name);
}
