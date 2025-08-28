package com.pragma.solicitudes.domain.usecase;


import com.pragma.solicitudes.domain.model.LoanApplication;
import com.pragma.solicitudes.domain.model.gateways.LoanApplicationGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanApplicationUseCaseTest {

    @Mock
    private LoanApplicationGateway loanApplicationGateway;

    @InjectMocks
    private LoanApplicationUseCase loanApplicationUseCase;

    private LoanApplicationRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new LoanApplicationRequest(
                "123456789",
                new BigDecimal("5000000"),
                12,
                "Personal"
        );
    }

    @Test
    void testProcessLoanApplicationWithValidRequest() {
        LoanApplication expectedApplication = new LoanApplication(
                "123456789",
                new BigDecimal("5000000"),
                12,
                "Personal"
        );

        when(loanApplicationGateway.save(any(LoanApplication.class)))
                .thenReturn(Mono.just(expectedApplication));

        StepVerifier.create(loanApplicationUseCase.processLoanApplication(validRequest))
                .expectNext(expectedApplication)
                .verifyComplete();
    }

    @Test
    void testProcessLoanApplicationWithInvalidLoanType() {
        LoanApplicationRequest invalidRequest = new LoanApplicationRequest(
                "123456789",
                new BigDecimal("5000000"),
                12,
                "InvalidType"
        );

        StepVerifier.create(loanApplicationUseCase.processLoanApplication(invalidRequest))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testProcessLoanApplicationWithNullDocument() {
        LoanApplicationRequest invalidRequest = new LoanApplicationRequest(
                null,
                new BigDecimal("5000000"),
                12,
                "Personal"
        );

        StepVerifier.create(loanApplicationUseCase.processLoanApplication(invalidRequest))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testProcessLoanApplicationWithEmptyDocument() {
        LoanApplicationRequest invalidRequest = new LoanApplicationRequest(
                "",
                new BigDecimal("5000000"),
                12,
                "Personal"
        );

        StepVerifier.create(loanApplicationUseCase.processLoanApplication(invalidRequest))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testProcessLoanApplicationWithAmountBelowMinimum() {
        LoanApplicationRequest invalidRequest = new LoanApplicationRequest(
                "123456789",
                new BigDecimal("0"),
                12,
                "Personal"
        );

        StepVerifier.create(loanApplicationUseCase.processLoanApplication(invalidRequest))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testProcessLoanApplicationWithNullAmount() {
        LoanApplicationRequest invalidRequest = new LoanApplicationRequest(
                "123456789",
                null,
                12,
                "Personal"
        );

        StepVerifier.create(loanApplicationUseCase.processLoanApplication(invalidRequest))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testProcessLoanApplicationWithNullTerm() {
        LoanApplicationRequest invalidRequest = new LoanApplicationRequest(
                "123456789",
                new BigDecimal("5000000"),
                null,
                "Personal"
        );

        StepVerifier.create(loanApplicationUseCase.processLoanApplication(invalidRequest))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testProcessLoanApplicationWithTermBelowMinimum() {
        LoanApplicationRequest invalidRequest = new LoanApplicationRequest(
                "123456789",
                new BigDecimal("5000000"),
                0,
                "Personal"
        );

        StepVerifier.create(loanApplicationUseCase.processLoanApplication(invalidRequest))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testProcessLoanApplicationWithNullLoanType() {
        LoanApplicationRequest invalidRequest = new LoanApplicationRequest(
                "123456789",
                new BigDecimal("5000000"),
                12,
                null
        );

        StepVerifier.create(loanApplicationUseCase.processLoanApplication(invalidRequest))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testProcessLoanApplicationWithEmptyLoanType() {
        LoanApplicationRequest invalidRequest = new LoanApplicationRequest(
                "123456789",
                new BigDecimal("5000000"),
                12,
                ""
        );

        StepVerifier.create(loanApplicationUseCase.processLoanApplication(invalidRequest))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testProcessLoanApplicationWithAmountAboveMaximum() {
        LoanApplicationRequest invalidRequest = new LoanApplicationRequest(
                "123456789",
                new BigDecimal("1000000000000001"),
                12,
                "Personal"
        );

        StepVerifier.create(loanApplicationUseCase.processLoanApplication(invalidRequest))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testProcessLoanApplicationWithTermAboveMaximum() {
        LoanApplicationRequest invalidRequest = new LoanApplicationRequest(
                "123456789",
                new BigDecimal("5000000"),
                361, // MAX + 1
                "Personal"
        );

        StepVerifier.create(loanApplicationUseCase.processLoanApplication(invalidRequest))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testProcessLoanApplicationCallsSave() {
        LoanApplication expectedApplication = new LoanApplication(
                "123456789",
                new BigDecimal("5000000"),
                12,
                "Personal"
        );

        when(loanApplicationGateway.save(any(LoanApplication.class)))
                .thenReturn(Mono.just(expectedApplication));

        StepVerifier.create(loanApplicationUseCase.processLoanApplication(validRequest))
                .expectNextMatches(app -> app.getClientDocument().equals("123456789"))
                .verifyComplete();
    }

}