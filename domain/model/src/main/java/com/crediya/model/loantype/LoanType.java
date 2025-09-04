package com.crediya.model.loantype;

import java.math.BigDecimal;

public class LoanType {
    private Integer loanTypeId;
    private String name;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal interestRate;
    private Boolean autoValidation;


    public Integer getLoanTypeId() {
        return loanTypeId;
    }
    public void setLoanTypeId(Integer loanTypeId) {
        this.loanTypeId = loanTypeId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public BigDecimal getMinAmount() {
        return minAmount;
    }
    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }
    public BigDecimal getMaxAmount() {
        return maxAmount;
    }
    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }
    public BigDecimal getInterestRate() {
        return interestRate;
    }
    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }
    public Boolean getAutoValidation() {
        return autoValidation;
    }
    public void setAutoValidation(Boolean autoValidation) {
        this.autoValidation = autoValidation;
    }
}

