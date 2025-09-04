package com.crediya.model.loantype.gateways;

import com.crediya.model.loantype.LoanType;
import reactor.core.publisher.Mono;

public interface LoanTypeRepository {
    Mono<LoanType> getLoanTypeByName(String name);
    Mono<LoanType> getLoanTypeById(Integer id);
}
