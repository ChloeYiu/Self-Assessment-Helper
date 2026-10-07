package com.helper.income.implementation.dividend.model;

import com.helper.income.implementation.Security;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Quantity of a security held as of a dated holding boundary.
 */
public class SecurityHoldingSnapshot {
    private final LocalDate snapshotDate;
    private final Security security;
    private final BigDecimal quantity;

    /**
     * Creates an assumed zero holding snapshot on 31 December before a reporting period starts.
     *
     * @param security held security
     * @param reportingPeriodStartDate reporting period start date
     * @return assumed zero holding snapshot
     */
    public static SecurityHoldingSnapshot assumedZeroBeforeReportingPeriod(
            Security security,
            LocalDate reportingPeriodStartDate
    ) {
        LocalDate startDate = Objects.requireNonNull(reportingPeriodStartDate, "reportingPeriodStartDate");
        return new SecurityHoldingSnapshot(
                LocalDate.of(startDate.getYear() - 1, 12, 31),
                security,
                BigDecimal.ZERO
        );
    }

    /**
     * Creates a holding snapshot for a security.
     *
     * @param snapshotDate date the quantity is known at, after movements on that date
     * @param security held security
     * @param quantity units held at the snapshot date
     */
    public SecurityHoldingSnapshot(
            LocalDate snapshotDate,
            Security security,
            BigDecimal quantity
    ) {
        this.snapshotDate = Objects.requireNonNull(snapshotDate, "snapshotDate");
        this.security = Objects.requireNonNull(security, "security");
        this.quantity = Objects.requireNonNull(quantity, "quantity").stripTrailingZeros();

        if (this.quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }
    }

    public LocalDate getSnapshotDate() {
        return snapshotDate;
    }

    public Security getSecurity() {
        return security;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
}
