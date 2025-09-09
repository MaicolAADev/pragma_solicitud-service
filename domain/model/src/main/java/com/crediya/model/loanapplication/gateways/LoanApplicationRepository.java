package com.crediya.model.loanapplication.gateways;

import com.crediya.model.loanapplication.LoanApplication;
import com.crediya.model.loanapplication.LoanApplicationWithExtras;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public interface LoanApplicationRepository {
    Mono<LoanApplication> save(LoanApplication loanApplication, Integer stateId, Integer loanTypeId);

    Flux<LoanApplicationWithExtras> findByStateIds(int page,
                                                   int size,
                                                   @Nullable String email,
                                                   @Nullable String loanType,
                                                   @Nullable String status);

    Flux<LoanApplicationWithExtras> findApprovedByIdentity(String identityDocument);

    Mono<LoanApplication> findById(String id);

    Mono<LoanApplication> updateStatusLoanApplication(String id, String idState);
}
