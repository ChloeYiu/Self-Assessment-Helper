package com.taxcalc.models;

import java.math.BigDecimal;

/**
 * Models interest from local savings accounts (ISAs, savings accounts, bonds, etc.)
 */
public class LocalSavingsInterest implements IncomeSource {
    
    public BigDecimal interestEarned;
    public BigDecimal accountBalance;
    public String accountType; // e.g., "ISA", "Savings Account", "Fixed Bond"
    public String bankName;
    public int taxYear;
    public String notes;
    
    public LocalSavingsInterest(BigDecimal interestEarned, String accountType, 
                               String bankName, int taxYear) {
        this.interestEarned = interestEarned;
        this.accountType = accountType;
        this.bankName = bankName;
        this.taxYear = taxYear;
        this.accountBalance = BigDecimal.ZERO;
        this.notes = "";
    }
    
    @Override
    public IncomeType getIncomeType() {
        return IncomeType.SAVINGS_INTEREST;
    }
    
    @Override
    public BigDecimal getGrossIncome() {
        return interestEarned;
    }
    
    @Override
    public int getTaxYear() {
        return taxYear;
    }
    
    @Override
    public BigDecimal calculateTaxableAmount() {
        // Personal Savings Allowance varies by income tax bracket
        // For basic rate taxpayer: £1,000
        // For higher rate: £500
        // For additional rate: £0
        // ISAs are tax-free, so no deduction needed
        
        if ("ISA".equalsIgnoreCase(accountType)) {
            return interestEarned; // Entire amount is tax-free
        }
        return BigDecimal.ZERO; // Will be handled by personal savings allowance in calculator
    }
    
}
