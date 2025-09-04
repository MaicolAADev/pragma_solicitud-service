package com.crediya.model.loanapplication;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationWithExtras {
    private Integer requestId;
    private BigDecimal amount;
    private Integer term;
    private String email;
    private String identityDocument;
    private String stateId;
    private String loanTypeId;
}
