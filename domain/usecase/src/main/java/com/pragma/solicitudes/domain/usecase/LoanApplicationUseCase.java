package com.pragma.solicitudes.domain.usecase;

import com.pragma.solicitudes.domain.model.LoanApplication;
import com.pragma.solicitudes.domain.model.LoanType;
import com.pragma.solicitudes.domain.model.gateways.LoanApplicationGateway;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

public class LoanApplicationUseCase {

    private final LoanApplicationGateway loanApplicationGateway;
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("1");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000000000000");
    private static final int MIN_TERM = 1;
    private static final int MAX_TERM = 360;

    public LoanApplicationUseCase(LoanApplicationGateway loanApplicationPort) {
        this.loanApplicationGateway = loanApplicationPort;
    }

    public Mono<LoanApplication> processLoanApplication(LoanApplicationRequest request) {
        return validateRequest(request)
                .flatMap(validatedRequest -> {
                    LoanApplication application = new LoanApplication(
                            validatedRequest.getClientDocument(),
                            validatedRequest.getAmount(),
                            validatedRequest.getTermMonths(),
                            validatedRequest.getLoanType()
                    );

                    if (!application.isValidLoanType(LoanType.getValidTypes())) {
                        return Mono.error(new IllegalArgumentException("Tipo de préstamo no válido"));
                    }

                    return loanApplicationGateway.save(application);
                });
    }

    private Mono<LoanApplicationRequest> validateRequest(LoanApplicationRequest request) {
        if (request.getClientDocument() == null || request.getClientDocument().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("El documento del cliente es obligatorio"));
        }

        if (request.getAmount() == null || request.getAmount().compareTo(MIN_AMOUNT) < 0) {
            return Mono.error(new IllegalArgumentException("El monto minimo es " + MIN_AMOUNT));
        }

        if (request.getAmount().compareTo(MAX_AMOUNT) > 0) {
            return Mono.error(new IllegalArgumentException("El monto maximo es " + MAX_AMOUNT));
        }

        if (request.getTermMonths() == null || request.getTermMonths() < MIN_TERM) {
            return Mono.error(new IllegalArgumentException("El termino minimo es " + MIN_TERM + " mes(es)"));
        }

        if (request.getTermMonths() > MAX_TERM) {
            return Mono.error(new IllegalArgumentException("El termino maximo es " + MAX_TERM + " mes(es)"));
        }

        if (request.getLoanType() == null || request.getLoanType().trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Loan type is required"));
        }

        return Mono.just(request);
    }
}