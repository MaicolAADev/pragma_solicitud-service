package com.crediya.model.loanapplication.gateways;

import com.crediya.model.loanapplication.LoanApplication;
import com.crediya.model.loanapplication.LoanApplicationExtended;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.List;

public interface LoanApplicationInputPort {
    Mono<LoanApplication> save(LoanApplication loanApplication);

    Mono<List<LoanApplicationExtended>> findByStates(
            int page, int size, @Nullable String email, @Nullable String loanType, @Nullable String status);
}
