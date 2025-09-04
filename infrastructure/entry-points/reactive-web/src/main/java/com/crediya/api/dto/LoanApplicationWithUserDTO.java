package com.crediya.api.dto;

import com.crediya.model.loanapplication.LoanApplicationExtended;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@Data
@AllArgsConstructor(staticName = "of")
@Builder
public class LoanApplicationWithUserDTO {
    @JsonProperty("id")
    private BigDecimal idRequest;

    @JsonProperty("monto")
    private BigDecimal amount;

    @JsonProperty("plazo")
    private Integer term;

    @JsonProperty("email")
    private String email;

    @JsonProperty("nombre")
    private String name;

    @JsonProperty("tipoPrestamo")
    private String loanType;

    @JsonProperty("tasaInteres")
    private BigDecimal interestRate;

    @JsonProperty("estadoSolicitud")
    private String applicationState;

    @JsonProperty("salarioBase")
    private BigDecimal baseSalary;

    @JsonProperty("deudaTotalMensual")
    private BigDecimal totalMonthlyDebt;


    public static LoanApplicationWithUserDTO of(LoanApplicationExtended app, UserDTO user) {
        return LoanApplicationWithUserDTO.builder()
                .idRequest(BigDecimal.valueOf(app.getBase().getRequestId()))
                .amount(app.getBase().getAmount())
                .term(app.getBase().getTerm())
                .email(app.getBase().getEmail())
                .loanType(app.getLoanTypeName())
                .applicationState(app.getStateName())
                .interestRate(app.getInterestRate())
                .baseSalary(user != null ? user.baseSalary() : null)
                .name(user != null ? user.name() + " " + user.lastName() : null)
                .totalMonthlyDebt(app.getTotalMonthlyDebt())
                .build();
    }
}
