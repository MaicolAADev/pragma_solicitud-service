package com.crediya.model.loanapplication;

import java.math.BigDecimal;

public class LoanApplicationExtended {
    private LoanApplicationWithExtras base;
    private BigDecimal interestRate;
    private BigDecimal totalMonthlyDebt;
    private String loanTypeName;
    private String stateName;

    public LoanApplicationExtended(LoanApplicationWithExtras base) {
        this.base = base;
    }

    public LoanApplicationWithExtras getBase() {
        return base;
    }

    public void setBase(LoanApplicationWithExtras base) {
        this.base = base;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getTotalMonthlyDebt() {
        return totalMonthlyDebt;
    }

    public void setTotalMonthlyDebt(BigDecimal totalMonthlyDebt) {
        this.totalMonthlyDebt = totalMonthlyDebt;
    }

    public String getLoanTypeName() {
        return loanTypeName;
    }

    public void setLoanTypeName(String loanTypeName) {
        this.loanTypeName = loanTypeName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }
}