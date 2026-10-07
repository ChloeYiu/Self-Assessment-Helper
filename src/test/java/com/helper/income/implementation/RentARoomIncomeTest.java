package com.helper.income.implementation;

import com.helper.config.TaxYear;
import com.helper.income.IncomeResult;
import com.helper.income.implementation.rentaroom.ExpenseType;
import com.helper.income.implementation.rentaroom.RentARoomCalculationMethod;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class RentARoomIncomeTest {

    @Test
    public void calculateResult_returnsRawRentBeforeLocalAdjustment() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), TaxYear.of(2025), BigDecimal.ONE);
        IncomeResult result = income.calculateResult();

        assertBigDecimalEquals("15000", result.getGrossIncome());
        assertBigDecimalEquals("7500", result.getAdjustedIncome());
    }

    @Test
    public void calculateResult_usesAllowanceWhenItIsBetter() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), TaxYear.of(2025), BigDecimal.ONE);

        BigDecimal adjustedIncome = income.calculateResult().getAdjustedIncome();

        assertBigDecimalEquals("7500", adjustedIncome);
        assertEquals(RentARoomCalculationMethod.ALLOWANCE, income.getPreferredCalculationMethod());
    }

    @Test
    public void calculateResult_usesActualExpensesWhenTheyReduceTaxableAmountMore() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), TaxYear.of(2025), BigDecimal.ONE);
        income.addExpense(ExpenseType.REPAIRS, new BigDecimal("9000"));

        BigDecimal adjustedIncome = income.calculateResult().getAdjustedIncome();

        assertBigDecimalEquals("6000", adjustedIncome);
        assertEquals(RentARoomCalculationMethod.ACTUAL_EXPENSES, income.getPreferredCalculationMethod());
    }

    @Test
    public void calculateResult_returnsZeroWhenAllowanceCoversAllProfit() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("5000"), TaxYear.of(2025), BigDecimal.ONE);

        BigDecimal adjustedIncome = income.calculateResult().getAdjustedIncome();

        assertBigDecimalEquals("0", adjustedIncome);
        assertEquals(RentARoomCalculationMethod.ALLOWANCE, income.getPreferredCalculationMethod());
    }

    @Test
    public void calculateResult_appliesHouseholdAllocationToExpenses() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), TaxYear.of(2025), new BigDecimal("0.5"));
        income.addExpense(ExpenseType.REPAIRS, new BigDecimal("8000"));

        BigDecimal adjustedIncome = income.calculateResult().getAdjustedIncome();

        assertBigDecimalEquals("7500", adjustedIncome);
        assertEquals(RentARoomCalculationMethod.ALLOWANCE, income.getPreferredCalculationMethod());
    }

    @Test
    public void getAdjustedIncomeWithActualExpenses_optsOutOfRentARoomAllowance() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), TaxYear.of(2025), new BigDecimal("0.5"));
        income.addExpense(ExpenseType.REPAIRS, new BigDecimal("8000"));

        BigDecimal actualExpenseMethodAdjusted = income.getAdjustedIncomeWithActualExpenses();

        // No rent-a-room allowance is applied: 8000 * 0.5 = 4000 expenses; 15000 - 4000 = 11000.
        assertBigDecimalEquals("11000", actualExpenseMethodAdjusted);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_throwsWhenGrossRentIsNull() {
        new RentARoomIncome(null, TaxYear.of(2025), BigDecimal.ONE);
    }

    @Test(expected = NullPointerException.class)
    public void addExpense_throwsWhenAmountIsNull() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), TaxYear.of(2025), BigDecimal.ONE);

        income.addExpense(ExpenseType.REPAIRS, null);
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
