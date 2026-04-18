package com.taxcalc.income.implementation;

import com.taxcalc.config.ExpenseType;
import com.taxcalc.income.Income;
import com.taxcalc.income.IncomeType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

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
    private BigDecimal standardAllowance = BigDecimal.valueOf(7500);
    
    /**
     * Full constructor with all fields
     */
    public RentARoomIncome(BigDecimal grossRent, int taxYear, BigDecimal householdAllocationPercentage) {
        this.grossRent = grossRent;
        this.expenses = new HashMap<>();
        this.householdAllocationPercentage = householdAllocationPercentage;
        this.taxYear = taxYear;
    }
    
    /**
     * Add an expense for a specific category (type-safe)
     */
    public void addExpense(ExpenseType type, BigDecimal amount) {
        expenses.put(type, amount);
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
    public BigDecimal calculateTaxableAmount() {
        BigDecimal allowanceMethod = calculateTaxableAmountWithAllowance();
        BigDecimal expensesMethod = calculateTaxableAmountWithActualExpenses();
        
        return allowanceMethod.min(expensesMethod); // Choose the method that gives lower taxable profit
    }

    public RentARoomCalculationMethod getPreferredCalculationMethod() {
        BigDecimal allowanceMethod = calculateTaxableAmountWithAllowance();
        BigDecimal expensesMethod = calculateTaxableAmountWithActualExpenses();
        
        return allowanceMethod.compareTo(expensesMethod) <= 0 ? 
            RentARoomCalculationMethod.ALLOWANCE : 
            RentARoomCalculationMethod.ACTUAL_EXPENSES;
    }
    
    public BigDecimal calculateTaxableAmountWithAllowance() {
        BigDecimal allowance = standardAllowance;
        BigDecimal taxableProfit = grossRent.subtract(allowance);
        return taxableProfit.max(BigDecimal.ZERO); // Cannot be negative
    }
    
    public BigDecimal calculateTaxableAmountWithActualExpenses() {
        BigDecimal total = expenses.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal allocatedExpenses = total.multiply(householdAllocationPercentage);
        BigDecimal taxableProfit = grossRent.subtract(allocatedExpenses);
        return taxableProfit.max(BigDecimal.ZERO); // Cannot be negative
    }
}

/**
 * Indicates which Rent-a-Room taxable amount calculation is preferred.
 */
enum RentARoomCalculationMethod {
    ALLOWANCE,
    ACTUAL_EXPENSES
}
