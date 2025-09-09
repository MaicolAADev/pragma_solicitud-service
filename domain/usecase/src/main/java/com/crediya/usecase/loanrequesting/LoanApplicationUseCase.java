package com.crediya.usecase.loanrequesting;

import com.crediya.model.loanapplication.LoanApplication;
import com.crediya.model.loanapplication.LoanApplicationExtended;
import com.crediya.model.loanapplication.gateways.LoanApplicationInputPort;
import com.crediya.model.loanapplication.gateways.LoanApplicationRepository;
import com.crediya.model.loantype.LoanType;
import com.crediya.model.loantype.gateways.LoanTypeRepository;
import com.crediya.model.state.State;
import com.crediya.model.state.gateways.StateRepository;
import com.crediya.usecase.exception.ArgumentException;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;


public class LoanApplicationUseCase implements LoanApplicationInputPort {
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final StateRepository stateRepository;
    private static final Logger logger = Logger.getLogger(LoanApplicationUseCase.class.getName());
    private static final String INITIAL_STATE = "Pendiente de revisión";

    public LoanApplicationUseCase(LoanApplicationRepository loanApplicationRepository, LoanTypeRepository loanTypeRepository, StateRepository stateRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanTypeRepository = loanTypeRepository;
        this.stateRepository = stateRepository;
    }

    @Override
    public Mono<LoanApplication> save(LoanApplication loanApplication) {
        logger.info("Iniciando registro de solicitud de préstamo para el usuario: " + loanApplication.getEmail());

        return Mono.zip(
                        loanTypeRepository.getLoanTypeByName(loanApplication.getLoanType())
                                .switchIfEmpty(Mono.error(new ArgumentException("El tipo de préstamo no existe"))),
                        stateRepository.getStateByName(INITIAL_STATE)
                                .switchIfEmpty(Mono.error(new ArgumentException("El estado inicial no existe")))
                ).flatMap(tuple -> {
                    LoanType loanTypeEntity = tuple.getT1();
                    State stateEntity = tuple.getT2();

                    validateAmount(loanApplication.getAmount());
                    validateAmountWithinLoanType(loanApplication.getAmount(), loanTypeEntity);

                    loanApplication.setState(INITIAL_STATE);

                    logger.info("Validaciones superadas: tipo de préstamo y estado inicial correctos");

                    return loanApplicationRepository.save(
                            loanApplication,
                            stateEntity.getStateId(),
                            loanTypeEntity.getLoanTypeId()
                    );
                })
                .doOnSuccess(saved -> logger.info("Solicitud de préstamo guardada correctamente con ID: " + saved.getRequestId()))
                .doOnError(error -> logger.severe("Error al guardar la solicitud de préstamo: " + error.getMessage()));
    }

    @Override
    public Mono<List<LoanApplicationExtended>> findByStates(
            int page, int size, @Nullable String email, @Nullable String loanType, @Nullable String status) {
        return loanApplicationRepository.findByStateIds(page, size, email, loanType, status)
                .flatMap(app ->
                        Mono.zip(
                                loanTypeRepository.getLoanTypeById(Integer.valueOf(app.getLoanTypeId()))
                                        .defaultIfEmpty(new LoanType()),
                                stateRepository.getStateById(app.getStateId())
                                        .defaultIfEmpty(new State())
                        ).map(tuple -> {
                            LoanType loanTypeEntity = tuple.getT1();
                            State state = tuple.getT2();

                            LoanApplicationExtended extended = new LoanApplicationExtended(app);
                            extended.setInterestRate(loanTypeEntity.getInterestRate());
                            extended.setLoanTypeName(loanTypeEntity.getName());
                            extended.setStateName(state.getName());
                            return extended;
                        }).flatMap(this::calculateDebt)
                )
                .collectList()
                .doOnError(error -> logger.severe("Error al obtener solicitudes de préstamo: " + error.getMessage()));
    }


    public Mono<LoanApplicationExtended> calculateDebt(LoanApplicationExtended app) {
        return loanApplicationRepository.findApprovedByIdentity(app.getBase().getIdentityDocument())
                .flatMap(loan ->
                        {
                            String loanTypeId = loan.getLoanTypeId();
                            if (loanTypeId == null)
                                return Mono.just(calculateMonthlyPayment(loan.getAmount(), loan.getTerm(), app.getInterestRate()));

                            return loanTypeRepository.getLoanTypeById(Integer.valueOf(loanTypeId))
                                    .map(loanType -> calculateMonthlyPayment(loan.getAmount(), loan.getTerm(), loanType.getInterestRate()))
                                    .defaultIfEmpty(calculateMonthlyPayment(loan.getAmount(), loan.getTerm(), app.getInterestRate()));
                        }
                )
                .collectList()
                .map(monthlyPayments -> {
                    BigDecimal totalDebt = monthlyPayments.stream()
                            .filter(Objects::nonNull)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    app.setTotalMonthlyDebt(totalDebt);
                    return app;
                });
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, Integer termMonths, BigDecimal monthlyRatePercent) {
        if (principal == null || termMonths == null || termMonths == 0) return BigDecimal.ZERO;

        BigDecimal monthlyRate = monthlyRatePercent == null
                ? BigDecimal.ZERO
                : monthlyRatePercent.divide(BigDecimal.valueOf(100), 18, RoundingMode.HALF_UP);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal pow = onePlusR.pow(termMonths);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(pow);
        BigDecimal denominator = pow.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private void validateAmount(BigDecimal amount) {
        Objects.requireNonNull(amount, "El monto no puede ser nulo");
        if (amount.signum() <= 0) {
            throw new ArgumentException("El monto debe ser mayor que cero");
        }
    }

    private void validateAmountWithinLoanType(BigDecimal amount, LoanType loanType) {
        if (amount.compareTo(loanType.getMinAmount()) < 0 || amount.compareTo(loanType.getMaxAmount()) > 0) {
            throw new ArgumentException(String.format(
                    "El monto %.2f no está dentro del rango permitido [%.2f - %.2f] para el tipo de préstamo %s",
                    amount, loanType.getMinAmount(), loanType.getMaxAmount(), loanType.getName()
            ));
        }
    }
}
