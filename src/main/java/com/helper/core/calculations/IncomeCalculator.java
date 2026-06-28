package com.helper.core.calculations;

import com.helper.core.income.Income;
import com.helper.core.income.IncomeType;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Calculates income summary values from income sources.
 */
public class IncomeCalculator {
    private final int taxYear;
    private final Map<IncomeType, List<Income>> incomeSources;

    public IncomeCalculator(int taxYear) {
        this.taxYear = taxYear;
        this.incomeSources = new HashMap<>();
    }

    public void addIncomeSources(IncomeType incomeType, Income source) {
        if (incomeType == null || source == null) {
            throw new IllegalArgumentException("incomeType and source must not be null");
        }
        incomeSources.computeIfAbsent(incomeType, k -> new ArrayList<>()).add(source);
    }

    public BigDecimal calculateAdjustedIncomeByType(IncomeType incomeType) {
        List<Income> sources = incomeSources.getOrDefault(
                Objects.requireNonNull(incomeType, "incomeType"),
                List.of()
        );
        return sources.stream()
                .map(Income::getAdjustedIncome)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTaxYear() {
        return taxYear;
    }
}
