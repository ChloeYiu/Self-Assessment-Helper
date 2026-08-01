package com.helper.income.implementation.capitalgain.model;

import com.helper.security.Security;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Excess reportable income already taxed as income and added to the CGT pool cost.
 */
public class EriAdjustment {
    private final LocalDate adjustmentDate;
    private final Security security;
    private final BigDecimal amountGbp;

    public EriAdjustment(
            LocalDate adjustmentDate,
            Security security,
            BigDecimal amountGbp
    ) {
        this.adjustmentDate = Objects.requireNonNull(adjustmentDate, "adjustmentDate");
        this.security = Objects.requireNonNull(security, "security");
        this.amountGbp = Objects.requireNonNull(amountGbp, "amountGbp");

        if (this.amountGbp.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amountGbp must not be negative");
        }
    }

    public LocalDate getAdjustmentDate() {
        return adjustmentDate;
    }

    public Security getSecurity() {
        return security;
    }

    public BigDecimal getAmountGbp() {
        return amountGbp;
    }
}
