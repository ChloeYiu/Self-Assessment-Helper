package com.taxcalc.models;

import java.math.BigDecimal;

/**
 * Models returns from managed investment accounts (Nutmeg, Robo-advisors, Fund supermarkets, etc.)
 */
public class ManagedInvestment implements IncomeSource {
    
    public BigDecimal dividendReceived;
    public BigDecimal capitalGain;
    public BigDecimal totalInvested;
    public String accountName;
    public String provider; // e.g., "Nutmeg", "Wealthsimple", "Moneyfarm"
    public String accountType; // e.g., "Stocks & Shares ISA", "General Investment Account"
    public int taxYear;
    public String notes;
    
    public ManagedInvestment(BigDecimal dividendReceived, String provider, 
                           String accountType, int taxYear) {
        this.dividendReceived = dividendReceived;
        this.provider = provider;
        this.accountType = accountType;
        this.taxYear = taxYear;
        this.capitalGain = BigDecimal.ZERO;
        this.totalInvested = BigDecimal.ZERO;
        this.notes = "";
    }
    
    @Override
    public IncomeType getIncomeType() {
        return IncomeType.MANAGED_INVESTMENT;
    }
    
    @Override
    public BigDecimal getGrossIncome() {
        return dividendReceived.add(capitalGain);
    }
    
    @Override
    public int getTaxYear() {
        return taxYear;
    }
    
    @Override
    public BigDecimal calculateTaxableAmount() {
        // If ISA, entire amount is tax-free
        if ("Stocks & Shares ISA".equalsIgnoreCase(accountType)) {
            return getGrossIncome();
        }
        
        // Otherwise, use dividend allowance for dividends
        BigDecimal dividendAllowance = BigDecimal.valueOf(500);
        BigDecimal allowance = BigDecimal.ZERO;
        
        if (dividendReceived.compareTo(dividendAllowance) <= 0) {
            allowance = allowance.add(dividendReceived);
        } else {
            allowance = allowance.add(dividendAllowance);
        }
        
        return allowance;
    }
    
}
