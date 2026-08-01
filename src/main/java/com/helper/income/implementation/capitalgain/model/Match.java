package com.helper.income.implementation.capitalgain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Acquisition quantity already matched to a disposal and therefore not added to the pool.
 */
public class Match {
    private final String acquisitionTradeId;
    private final String disposalTradeId;
    private final BigDecimal quantity;
    private final BigDecimal costGbp;
    private final MatchType matchType;

    public Match(
            String acquisitionTradeId,
            String disposalTradeId,
            BigDecimal quantity,
            BigDecimal costGbp,
            MatchType matchType
    ) {
        this.acquisitionTradeId = Objects.requireNonNull(acquisitionTradeId, "acquisitionTradeId");
        this.disposalTradeId = Objects.requireNonNull(disposalTradeId, "disposalTradeId");
        this.quantity = Objects.requireNonNull(quantity, "quantity");
        this.costGbp = Objects.requireNonNull(costGbp, "costGbp");
        this.matchType = Objects.requireNonNull(matchType, "matchType");

        if (this.acquisitionTradeId.isBlank()) {
            throw new IllegalArgumentException("acquisitionTradeId must not be blank");
        }
        if (this.disposalTradeId.isBlank()) {
            throw new IllegalArgumentException("disposalTradeId must not be blank");
        }
        if (this.quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        if (this.costGbp.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("costGbp must not be negative");
        }
    }

    public String getAcquisitionTradeId() {
        return acquisitionTradeId;
    }

    public String getDisposalTradeId() {
        return disposalTradeId;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getCostGbp() {
        return costGbp;
    }

    public MatchType getMatchType() {
        return matchType;
    }
}
