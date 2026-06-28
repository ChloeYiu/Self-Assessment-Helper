package com.helper.core.income.implementation;

import com.helper.core.income.Income;
import com.helper.core.income.IncomeType;
import com.helper.core.income.implementation.savings.Saving;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate for storing multiple savings income sources.
 */
public class SavingIncome implements Income {

    public BigDecimal grossSavingIncome;
    public int taxYear;
    private final List<Saving> savingSources;

    public SavingIncome(int taxYear) {
        this.grossSavingIncome = BigDecimal.ZERO;
        this.taxYear = taxYear;
        this.savingSources = new ArrayList<>();
    }

    @Override
    public IncomeType getIncomeType() {
        return IncomeType.SAVINGS;
    }

    @Override
    public BigDecimal getGrossIncome() {
        if (savingSources.isEmpty()) {
            return grossSavingIncome;
        }

        grossSavingIncome = savingSources.stream()
            .map(Saving::calculateSavingAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return grossSavingIncome;
    }

    @Override
    public int getTaxYear() {
        return taxYear;
    }

    public void addSavingIncome(Saving savingSource) {
        if (savingSource != null) {
            savingSources.add(savingSource);
        }
    }

    @Override
    public BigDecimal calculateTaxableAmount() {
        BigDecimal grossIncome = getGrossIncome();
        BigDecimal personalSavingsAllowance = getPersonalSavingsAllowance();
        return grossIncome.subtract(personalSavingsAllowance).max(BigDecimal.ZERO);
    }

    private BigDecimal getPersonalSavingsAllowance() {
        // Placeholder for actual allowance logic based on tax bands
        return BigDecimal.valueOf(1000); // Example fixed allowance
    }
}
