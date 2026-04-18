package com.taxcalc.calculations;

import com.taxcalc.config.TaxYearPeriod;
import com.taxcalc.income.Income;
import com.taxcalc.income.IncomeType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Main tax calculation engine
 * Calculates income tax owed based on UK tax brackets and allowances
 */
public class TaxCalculator { 
    private int taxYear;
    private final Map<IncomeType, List<Income>> incomeSources;
    
    public TaxCalculator(int taxYear) {
        this.taxYear = taxYear;
        this.incomeSources = new HashMap<>();
    }

    public void addIncomeSources(IncomeType incomeType, Income source) {
        if (incomeType == null || source == null) {
            throw new IllegalArgumentException("incomeType and source must not be null");
        }
        incomeSources.computeIfAbsent(incomeType, k -> new ArrayList<>()).add(source);
    }

    public BigDecimal calculateTaxByType(IncomeType incomeType) {
        List<Income> sources = incomeSources.getOrDefault(incomeType, List.of());
        return sources.stream()
            .map(Income::calculateTaxableAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}