package com.helper.core.calculations;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Calculates tax-free allowance amounts from wider income context.
 */
public class AllowanceCalculator {

    private static final BigDecimal BASIC_RATE_LIMIT = BigDecimal.valueOf(50270);
    private static final BigDecimal ADDITIONAL_RATE_THRESHOLD = BigDecimal.valueOf(125140);
    private static final BigDecimal BASIC_RATE_PERSONAL_SAVINGS_ALLOWANCE = BigDecimal.valueOf(1000);
    private static final BigDecimal HIGHER_RATE_PERSONAL_SAVINGS_ALLOWANCE = BigDecimal.valueOf(500);
    private static final BigDecimal ADDITIONAL_RATE_PERSONAL_SAVINGS_ALLOWANCE = BigDecimal.ZERO;

    public BigDecimal getPersonalSavingsAllowance(BigDecimal totalIncome) {
        Objects.requireNonNull(totalIncome, "totalIncome");

        if (totalIncome.compareTo(BASIC_RATE_LIMIT) <= 0) {
            return BASIC_RATE_PERSONAL_SAVINGS_ALLOWANCE;
        }
        if (totalIncome.compareTo(ADDITIONAL_RATE_THRESHOLD) <= 0) {
            return HIGHER_RATE_PERSONAL_SAVINGS_ALLOWANCE;
        }
        return ADDITIONAL_RATE_PERSONAL_SAVINGS_ALLOWANCE;
    }
}
