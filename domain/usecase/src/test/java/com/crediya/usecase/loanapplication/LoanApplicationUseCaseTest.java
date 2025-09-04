package com.crediya.usecase.loanapplication;

import com.crediya.model.loanapplication.LoanApplication;
import com.crediya.model.loanapplication.LoanApplicationExtended;
import com.crediya.model.loanapplication.LoanApplicationWithExtras;
import com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import com.crediya.model.loantype.LoanType;
import com.crediya.model.loantype.gateways.LoanTypeRepository;
import com.crediya.model.state.State;
import com.crediya.model.state.gateways.StateRepository;
import com.crediya.usecase.loanrequesting.LoanApplicationUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LoanApplicationUseCaseTest {

    private LoanApplicationRepository loanApplicationRepository;
    private LoanTypeRepository loanTypeRepository;
    private StateRepository stateRepository;
    private LoanApplicationUseCase useCase;

    @BeforeEach
    void setUp() {
        loanApplicationRepository = mock(LoanApplicationRepository.class);
        loanTypeRepository = mock(LoanTypeRepository.class);
        stateRepository = mock(StateRepository.class);
        useCase = new LoanApplicationUseCase(loanApplicationRepository, loanTypeRepository, stateRepository);
    }

    private LoanType buildLoanType(BigDecimal min, BigDecimal max) {
        LoanType loanType = new LoanType();
        loanType.setLoanTypeId(1);
        loanType.setName("Personal");
        loanType.setMinAmount(min);
        loanType.setMaxAmount(max);
        return loanType;
    }

    private State buildState() {
        State state = new State();
        state.setStateId(100);
        state.setName("Pendiente de revisión");
        return state;
    }

    private LoanApplication buildApplication(BigDecimal amount) {
        LoanApplication app = new LoanApplication();
        app.setRequestId(200);
        app.setEmail("user@test.com");
        app.setLoanType("Personal");
        app.setAmount(amount);
        return app;
    }

    @Test
    void shouldSaveLoanApplicationSuccessfully() {
        LoanApplication application = buildApplication(BigDecimal.valueOf(500));
        LoanType loanType = buildLoanType(BigDecimal.valueOf(100), BigDecimal.valueOf(1000));
        State state = buildState();

        when(loanTypeRepository.getLoanTypeByName("Personal")).thenReturn(Mono.just(loanType));
        when(stateRepository.getStateByName("Pendiente de revisión")).thenReturn(Mono.just(state));
        when(loanApplicationRepository.save(any(), eq(state.getStateId()), eq(loanType.getLoanTypeId())))
                .thenAnswer(invocation -> {
                    LoanApplication saved = invocation.getArgument(0);
                    saved.setRequestId(999);
                    return Mono.just(saved);
                });

        StepVerifier.create(useCase.save(application))
                .expectNextMatches(saved -> saved.getRequestId() == 999 && "Pendiente de revisión".equals(saved.getState()))
                .verifyComplete();

        verify(loanApplicationRepository, times(1)).save(any(), any(), any());
    }

    @Test
    void shouldFailWhenLoanTypeDoesNotExist() {
        LoanApplication application = buildApplication(BigDecimal.valueOf(500));
        when(loanTypeRepository.getLoanTypeByName("Personal")).thenReturn(Mono.empty());
        when(stateRepository.getStateByName("Pendiente de revisión")).thenReturn(Mono.just(buildState()));

        StepVerifier.create(useCase.save(application))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().contains("tipo de préstamo"))
                .verify();
    }

    @Test
    void shouldFailWhenStateDoesNotExist() {
        LoanApplication application = buildApplication(BigDecimal.valueOf(500));
        when(loanTypeRepository.getLoanTypeByName("Personal")).thenReturn(Mono.just(buildLoanType(BigDecimal.valueOf(100), BigDecimal.valueOf(1000))));
        when(stateRepository.getStateByName("Pendiente de revisión")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.save(application))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().contains("estado inicial"))
                .verify();
    }

    @Test
    void shouldFailWhenAmountIsNull() {
        LoanApplication application = buildApplication(null);
        when(loanTypeRepository.getLoanTypeByName("Personal"))
                .thenReturn(Mono.just(buildLoanType(BigDecimal.valueOf(100), BigDecimal.valueOf(1000))));
        when(stateRepository.getStateByName("Pendiente de revisión"))
                .thenReturn(Mono.just(buildState()));

        StepVerifier.create(useCase.save(application))
                .expectErrorMatches(e -> e instanceof NullPointerException
                        && e.getMessage().contains("El monto no puede ser nulo"))
                .verify();
    }

    @Test
    void shouldFailWhenAmountIsZeroOrNegative() {
        LoanApplication application = buildApplication(BigDecimal.ZERO);
        when(loanTypeRepository.getLoanTypeByName("Personal")).thenReturn(Mono.just(buildLoanType(BigDecimal.valueOf(100), BigDecimal.valueOf(1000))));
        when(stateRepository.getStateByName("Pendiente de revisión")).thenReturn(Mono.just(buildState()));

        StepVerifier.create(useCase.save(application))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldFailWhenAmountLessThanMin() {
        LoanApplication application = buildApplication(BigDecimal.valueOf(50));
        LoanType loanType = buildLoanType(BigDecimal.valueOf(100), BigDecimal.valueOf(1000));
        when(loanTypeRepository.getLoanTypeByName("Personal")).thenReturn(Mono.just(loanType));
        when(stateRepository.getStateByName("Pendiente de revisión")).thenReturn(Mono.just(buildState()));

        StepVerifier.create(useCase.save(application))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException && e.getMessage().contains("no está dentro del rango"))
                .verify();
    }

    @Test
    void shouldFailWhenAmountGreaterThanMax() {
        LoanApplication application = buildApplication(BigDecimal.valueOf(2000));
        LoanType loanType = buildLoanType(BigDecimal.valueOf(100), BigDecimal.valueOf(1000));
        when(loanTypeRepository.getLoanTypeByName("Personal")).thenReturn(Mono.just(loanType));
        when(stateRepository.getStateByName("Pendiente de revisión")).thenReturn(Mono.just(buildState()));

        StepVerifier.create(useCase.save(application))
                .expectErrorMatches(e -> e instanceof IllegalArgumentException && e.getMessage().contains("no está dentro del rango"))
                .verify();
    }

    @Test
    void shouldFindLoanApplicationsSuccessfully() {
        LoanApplicationWithExtras baseApp = new LoanApplicationWithExtras();
        baseApp.setLoanTypeId("1");                // importante
        baseApp.setStateId("1");                     // importante
        baseApp.setIdentityDocument("123456");     // importante

        LoanType loanType = buildLoanType(BigDecimal.valueOf(100), BigDecimal.valueOf(1000));
        loanType.setLoanTypeId(1);
        loanType.setInterestRate(BigDecimal.valueOf(10));

        State state = buildState();
        state.setStateId(1);
        state.setName("Pendiente");

        when(loanApplicationRepository.findByStateIds(eq(0), eq(10), any(), any(), any()))
                .thenReturn(Flux.just(baseApp));
        when(loanTypeRepository.getLoanTypeById(1))
                .thenReturn(Mono.just(loanType));
        when(stateRepository.getStateById(String.valueOf(1)))
                .thenReturn(Mono.just(state));
        when(loanApplicationRepository.findApprovedByIdentity("123456"))
                .thenReturn(Flux.empty());

        StepVerifier.create(useCase.findByStates(0, 10, null, null, null))
                .expectNextMatches(list -> !list.isEmpty()
                        && list.get(0).getLoanTypeName().equals("Personal")
                        && list.get(0).getStateName().equals("Pendiente"))
                .verifyComplete();
    }

    @Test
    void shouldHandleMissingLoanTypeOrStateInFindByStates() {
        LoanApplicationWithExtras baseApp = new LoanApplicationWithExtras();
        baseApp.setLoanTypeId("99");              // no existe
        baseApp.setStateId("99");                   // no existe
        baseApp.setIdentityDocument("ABC");

        when(loanApplicationRepository.findByStateIds(eq(0), eq(5), any(), any(), any()))
                .thenReturn(Flux.just(baseApp));
        when(loanTypeRepository.getLoanTypeById(99))
                .thenReturn(Mono.empty()); // LoanType vacío
        when(stateRepository.getStateById(String.valueOf(99)))
                .thenReturn(Mono.empty()); // State vacío
        when(loanApplicationRepository.findApprovedByIdentity("ABC"))
                .thenReturn(Flux.empty());

        StepVerifier.create(useCase.findByStates(0, 5, null, null, null))
                .expectNextMatches(list -> !list.isEmpty()
                        && list.getFirst().getLoanTypeName() == null
                        && list.getFirst().getStateName() == null)
                .verifyComplete();
    }

    @Test
    void shouldReturnZeroWhenPrincipalOrTermInvalid() throws Exception {
        BigDecimal result1 = useCaseTestHelper_calculateMonthlyPayment(null, 12, BigDecimal.TEN);
        BigDecimal result2 = useCaseTestHelper_calculateMonthlyPayment(BigDecimal.TEN, null, BigDecimal.TEN);
        BigDecimal result3 = useCaseTestHelper_calculateMonthlyPayment(BigDecimal.TEN, 0, BigDecimal.TEN);

        assertEquals(BigDecimal.ZERO, result1);
        assertEquals(BigDecimal.ZERO, result2);
        assertEquals(BigDecimal.ZERO, result3);
    }

    @Test
    void shouldCalculateMonthlyPaymentWithoutInterest() throws Exception {
        BigDecimal result = useCaseTestHelper_calculateMonthlyPayment(BigDecimal.valueOf(1200), 12, null);
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), result);
    }

    @Test
    void shouldCalculateMonthlyPaymentWithInterest() throws Exception {
        BigDecimal result = useCaseTestHelper_calculateMonthlyPayment(BigDecimal.valueOf(1000), 12, BigDecimal.valueOf(5));
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    private BigDecimal useCaseTestHelper_calculateMonthlyPayment(BigDecimal principal, Integer term, BigDecimal rate) throws Exception {
        var method = LoanApplicationUseCase.class.getDeclaredMethod("calculateMonthlyPayment", BigDecimal.class, Integer.class, BigDecimal.class);
        method.setAccessible(true);
        return (BigDecimal) method.invoke(useCase, principal, term, rate);
    }


}

