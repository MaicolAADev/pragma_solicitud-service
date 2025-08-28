package com.pragma.solicitudes.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationRequestDto {

    @NotBlank(message = "Client document is required")
    private String clientDocument;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "100000", message = "Minimum amount is 100,000")
    @Digits(integer = 12, fraction = 2, message = "Amount must have maximum 12 integers and 2 decimals")
    private BigDecimal amount;

    @NotNull(message = "Term in months is required")
    @Positive(message = "Term must be a positive number")
    private Integer termMonths;

    @NotBlank(message = "Loan type is required")
    private String loanType;
}