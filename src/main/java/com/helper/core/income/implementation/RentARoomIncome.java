package com.helper.core.income.implementation;

import com.helper.core.config.ExpenseType;
import com.helper.core.income.Income;
import com.helper.core.income.IncomeType;
import java.math.BigDecimal;
import java.util.HashMap;
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
    public int taxYear;
    // TODO: Load this from AllowanceCalculator/tax-year allowance configuration instead of hardcoding it.
    private final BigDecimal standardAllowance = BigDecimal.valueOf(7500);

    /**
     * Full constructor with all fields
     */
    public RentARoomIncome(BigDecimal grossRent, int taxYear, BigDecimal householdAllocationPercentage) {
        this.grossRent = Objects.requireNonNull(grossRent, "grossRent");
        this.expenses = new HashMap<>();
        this.householdAllocationPercentage = Objects.requireNonNull(
            householdAllocationPercentage,
            "householdAllocationPercentage"
        );
        this.taxYear = taxYear;
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
    public BigDecimal getGrossIncome() {
        return grossRent;
    }
    
    @Override
    public int getTaxYear() {
        return taxYear;
    }

    @Override
    public BigDecimal getAdjustedIncome() {
        BigDecimal allowanceMethod = getAdjustedIncomeWithAllowance();
        BigDecimal expensesMethod = getAdjustedIncomeWithActualExpenses();

        return allowanceMethod.min(expensesMethod);
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
