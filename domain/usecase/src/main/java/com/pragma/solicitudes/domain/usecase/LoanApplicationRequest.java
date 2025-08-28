package com.pragma.solicitudes.domain.usecase;

import java.math.BigDecimal;

public class LoanApplicationRequest {
    private String clientDocument;
    private BigDecimal amount;
    private Integer termMonths;
    private String loanType;

    public LoanApplicationRequest(String clientDocument, BigDecimal amount, Integer termMonths, String loanType) {
        this.clientDocument = clientDocument;
        this.amount = amount;
        this.termMonths = termMonths;
        this.loanType = loanType;
    }

    public String getClientDocument() {
        return clientDocument;
    }

    public void setClientDocument(String clientDocument) {
        this.clientDocument = clientDocument;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }
}
