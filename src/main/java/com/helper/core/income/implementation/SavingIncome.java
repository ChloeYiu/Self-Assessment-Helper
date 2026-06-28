package com.helper.core.income.implementation;

import com.helper.core.calculations.AllowanceCalculator;
import com.helper.core.income.Income;
import com.helper.core.income.IncomeType;
import com.helper.core.income.implementation.savings.Saving;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate for storing multiple savings income sources.
 */
public class SavingIncome implements Income {

    public BigDecimal grossSavingIncome;
    public int taxYear;
    private final List<Saving> savingSources;
    private final AllowanceCalculator allowanceCalculator;

    public SavingIncome(int taxYear) {
        this(taxYear, new AllowanceCalculator());
    }

    public SavingIncome(int taxYear, AllowanceCalculator allowanceCalculator) {
        this.grossSavingIncome = BigDecimal.ZERO;
        this.taxYear = taxYear;
        this.savingSources = new ArrayList<>();
        this.allowanceCalculator = Objects.requireNonNull(allowanceCalculator, "allowanceCalculator");
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
        Saving source = Objects.requireNonNull(savingSource, "savingSource");
        if (source.getTaxYear() != taxYear) {
            throw new IllegalArgumentException("savingSource tax year must match aggregate tax year");
        }
        savingSources.add(source);
    }

    @Override
    public BigDecimal calculateTaxableAmount() {
        return calculateTaxableAmount(getGrossIncome());
    }

    public BigDecimal calculateTaxableAmount(BigDecimal totalIncome) {
        BigDecimal grossIncome = getGrossIncome();
        BigDecimal personalSavingsAllowance = allowanceCalculator.getPersonalSavingsAllowance(totalIncome);
        return grossIncome.subtract(personalSavingsAllowance).max(BigDecimal.ZERO);
    }
}
