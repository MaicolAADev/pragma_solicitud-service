package com.pragma.solicitudes.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LoanApplication {
    private Long id;
    private String clientDocument;
    private BigDecimal amount;
    private Integer termMonths;
    private String loanType;
    private String status;
    private LocalDateTime applicationDate;

    public LoanApplication(String clientDocument, BigDecimal amount, Integer termMonths, String loanType) {
        this.clientDocument = clientDocument;
        this.amount = amount;
        this.termMonths = termMonths;
        this.loanType = loanType;
        this.status = "Pending Review";
        this.applicationDate = LocalDateTime.now();
    }

    public LoanApplication(Long id, String clientDocument, BigDecimal amount, Integer termMonths,
                           String loanType, String status, LocalDateTime applicationDate) {
        this.id = id;
        this.clientDocument = clientDocument;
        this.amount = amount;
        this.termMonths = termMonths;
        this.loanType = loanType;
        this.status = status;
        this.applicationDate = applicationDate;
    }

    public LoanApplication() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }

    public boolean isValidLoanType(String[] validLoanTypes) {
        if (validLoanTypes == null || this.loanType == null) {
            return false;
        }

        for (String validType : validLoanTypes) {
            if (validType.equalsIgnoreCase(this.loanType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LoanApplication that = (LoanApplication) o;

        if (!clientDocument.equals(that.clientDocument)) return false;
        if (!amount.equals(that.amount)) return false;
        if (!termMonths.equals(that.termMonths)) return false;
        return loanType.equals(that.loanType);
    }

}