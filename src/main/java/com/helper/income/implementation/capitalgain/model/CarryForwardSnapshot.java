package com.helper.income.implementation.capitalgain.model;

import com.helper.config.TaxYear;
import com.helper.income.implementation.capitalgain.internal.PoolState;
import com.helper.income.implementation.Security;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * State carried from one capital-gain calculation into the next tax year.
 */
public class CarryForwardSnapshot {
    private final TaxYear taxYear;
    private final Security security;
    private final BigDecimal quantity;
    private final BigDecimal pooledCostGbp;
    private final List<Match> consumedAcquisitions;

    /**
     * Creates an assumed zero carry-forward snapshot for a tax year and security.
     */
    public static CarryForwardSnapshot assumedZero(TaxYear taxYear, Security security) {
        return new CarryForwardSnapshot(taxYear, security, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public CarryForwardSnapshot(
            TaxYear taxYear,
            Security security,
            BigDecimal quantity,
            BigDecimal pooledCostGbp
    ) {
        this(taxYear, security, quantity, pooledCostGbp, List.of());
    }

    public CarryForwardSnapshot(
            TaxYear taxYear,
            Security security,
            BigDecimal quantity,
            BigDecimal pooledCostGbp,
            List<Match> consumedAcquisitions
    ) {
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
        this.security = Objects.requireNonNull(security, "security");
        this.quantity = Objects.requireNonNull(quantity, "quantity").stripTrailingZeros();
        this.pooledCostGbp = Objects.requireNonNull(pooledCostGbp, "pooledCostGbp").setScale(2, RoundingMode.HALF_UP);
        this.consumedAcquisitions = List.copyOf(Objects.requireNonNull(consumedAcquisitions, "consumedAcquisitions"));

        if (this.quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }
        if (this.pooledCostGbp.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("pooledCostGbp must not be negative");
        }
        this.consumedAcquisitions.forEach(match -> Objects.requireNonNull(match, "consumedAcquisition"));
    }

    public TaxYear getTaxYear() {
        return taxYear;
    }

    public Security getSecurity() {
        return security;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getPooledCostGbp() {
        return pooledCostGbp;
    }

    public List<Match> getConsumedAcquisitions() {
        return List.copyOf(consumedAcquisitions);
    }

    public PoolState createPoolState() {
        return new PoolState(quantity, pooledCostGbp);
    }
}
