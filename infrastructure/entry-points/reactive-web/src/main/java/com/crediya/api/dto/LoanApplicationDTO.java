package com.crediya.api.dto;

import java.math.BigDecimal;

public record LoanApplicationDTO(
        String requestId,
        BigDecimal amount,
        Integer term,
        String email,
        String loanType,
        String identityDocument,
        String state
) {
}