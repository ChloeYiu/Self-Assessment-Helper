package com.helper.core.calculations;

import com.helper.core.income.Income;
import com.helper.core.income.IncomeType;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Calculates Self Assessment helper values from income sources.
 */
public class Calculator {
    private final int taxYear;
    private final Map<IncomeType, List<Income>> incomeSources;

    public Calculator(int taxYear) {
        this.taxYear = taxYear;
        this.incomeSources = new HashMap<>();
    }

    public void addIncomeSources(IncomeType incomeType, Income source) {
        if (incomeType == null || source == null) {
            throw new IllegalArgumentException("incomeType and source must not be null");
        }
        incomeSources.computeIfAbsent(incomeType, k -> new ArrayList<>()).add(source);
    }

    public BigDecimal calculateGrossIncomeByType(IncomeType incomeType) {
        List<Income> sources = incomeSources.getOrDefault(incomeType, List.of());
        return sources.stream()
                .map(Income::getGrossIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTaxYear() {
        return taxYear;
    }
}
