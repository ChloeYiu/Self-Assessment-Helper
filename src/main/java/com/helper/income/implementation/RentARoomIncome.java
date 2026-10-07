package com.helper.income.implementation;

import com.helper.config.TaxYear;
import com.helper.income.Income;
import com.helper.income.IncomeResult;
import com.helper.income.IncomeType;
import com.helper.income.implementation.rentaroom.ExpenseType;
import com.helper.income.implementation.rentaroom.RentARoomCalculationMethod;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Models income from Rent-a-Room scheme
 * Supports both standard allowance (£7,500) and actual expenses calculation
 * 
 * Expense categories are type-safe (ExpenseType enum)
 * Validation happens in code
 */
public class RentARoomIncome implements Income {
    
    public BigDecimal grossRent;
    public Map<ExpenseType, BigDecimal> expenses; // Type-safe categorized expenses
    public BigDecimal householdAllocationPercentage; // E.g., 0.5 = 50% if sharing with one other person
    public TaxYear taxYear;
    // TODO: Load this from tax-year allowance configuration instead of hardcoding it.
    private final BigDecimal standardAllowance = BigDecimal.valueOf(7500);

    /**
     * Full constructor with all fields
     */
    public RentARoomIncome(BigDecimal grossRent, TaxYear taxYear, BigDecimal householdAllocationPercentage) {
        this.grossRent = Objects.requireNonNull(grossRent, "grossRent");
        this.expenses = new HashMap<>();
        this.householdAllocationPercentage = Objects.requireNonNull(
            householdAllocationPercentage,
            "householdAllocationPercentage"
        );
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
    }
    
    /**
     * Add an expense for a specific category (type-safe)
     */
    public void addExpense(ExpenseType type, BigDecimal amount) {
        expenses.put(
            Objects.requireNonNull(type, "type"),
            Objects.requireNonNull(amount, "amount")
        );
    }
    
    @Override
    public IncomeType getIncomeType() {
        return IncomeType.RENT_A_ROOM;
    }
    
    @Override
    public IncomeResult calculateResult() {
        BigDecimal allowanceMethod = getAdjustedIncomeWithAllowance();
        BigDecimal expensesMethod = getAdjustedIncomeWithActualExpenses();

        return new IncomeResult(getIncomeType(), grossRent, allowanceMethod.min(expensesMethod), List.of());
    }
    
    @Override
    public TaxYear getTaxYear() {
        return taxYear;
    }

    public RentARoomCalculationMethod getPreferredCalculationMethod() {
        BigDecimal allowanceMethod = getAdjustedIncomeWithAllowance();
        BigDecimal expensesMethod = getAdjustedIncomeWithActualExpenses();

        return allowanceMethod.compareTo(expensesMethod) <= 0
                ? RentARoomCalculationMethod.ALLOWANCE
                : RentARoomCalculationMethod.ACTUAL_EXPENSES;
    }

    public BigDecimal getAdjustedIncomeWithAllowance() {
        return grossRent.subtract(standardAllowance).max(BigDecimal.ZERO);
    }

    public BigDecimal getAdjustedIncomeWithActualExpenses() {
        BigDecimal total = expenses.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal allocatedExpenses = total.multiply(householdAllocationPercentage);
        return grossRent.subtract(allocatedExpenses).max(BigDecimal.ZERO);
    }
}
