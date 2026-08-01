package com.helper.core.calculations;

import com.helper.core.config.TaxYear;
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
    private final TaxYear taxYear;
    private final Map<IncomeType, List<Income>> incomeSources;

    public IncomeCalculator(TaxYear taxYear) {
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
        this.incomeSources = new HashMap<>();
    }

    public void addIncome(Income income) {
        Income value = Objects.requireNonNull(income, "income");
        if (!value.getTaxYear().equals(taxYear)) {
            throw new IllegalArgumentException("income tax year must match calculator tax year");
        }
        incomeSources
                .computeIfAbsent(value.getIncomeType(), ignored -> new ArrayList<>())
                .add(value);
    }

    public void addIncomeSources(IncomeType incomeType, Income source) {
        if (!Objects.requireNonNull(incomeType, "incomeType").equals(
                Objects.requireNonNull(source, "source").getIncomeType()
        )) {
            throw new IllegalArgumentException("incomeType must match source income type");
        }
        addIncome(source);
    }

    public BigDecimal calculateAdjustedIncomeByType(IncomeType incomeType) {
        return incomeSources
                .getOrDefault(Objects.requireNonNull(incomeType, "incomeType"), List.of())
                .stream()
                .map(income -> income.calculateResult().getAdjustedIncome())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public TaxYear getTaxYear() {
        return taxYear;
    }
}
