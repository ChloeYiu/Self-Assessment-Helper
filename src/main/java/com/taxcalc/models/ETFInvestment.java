package com.taxcalc.models;

import java.math.BigDecimal;

/**
 * Models dividends and gains from ETF investments
 */
public class ETFInvestment implements IncomeSource {
    
    public enum ETFDistributionType {
        ACCUMULATING,    // Dividends reinvested automatically
        DISTRIBUTING     // Dividends paid out
    }
    
    public enum ETFDomicile {
        UK,
        NON_UK
    }
    
    public BigDecimal dividendReceived;
    public BigDecimal capitalGain;
    public BigDecimal totalInvestment;
    public String etfName;
    public String etfSymbol;
    public String broker;
    public ETFDistributionType distributionType;
    public ETFDomicile domicile;
    public int taxYear;
    public String notes;
    
    public ETFInvestment(BigDecimal capitalGain, String etfName, String etfSymbol,
                        String broker, ETFDistributionType distributionType, 
                        ETFDomicile domicile, int taxYear) {
        this.capitalGain = capitalGain;
        this.dividendReceived = BigDecimal.ZERO; // Accumulating ETFs don't have dividend income
        this.etfName = etfName;
        this.etfSymbol = etfSymbol;
        this.broker = broker;
        this.distributionType = distributionType;
        this.domicile = domicile;
        this.taxYear = taxYear;
        this.totalInvestment = BigDecimal.ZERO;
        this.notes = "";
    }
    
    @Override
    public IncomeType getIncomeType() {
        return IncomeType.ETF_DIVIDEND;
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
        // Dividend allowance for 2024/25 is £500
        BigDecimal dividendAllowance = BigDecimal.valueOf(500);
        
        if (dividendReceived.compareTo(dividendAllowance) <= 0) {
            return dividendReceived; // All dividends are within allowance
        }
        return dividendAllowance;
    }
    
}
