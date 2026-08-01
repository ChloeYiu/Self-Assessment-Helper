package com.helper.income.implementation.capitalgain.model;

import com.helper.security.Security;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Normalized trade event after broker ingestion and FX conversion.
 */
public class Trade {
    private final String tradeId;
    private final LocalDate transactionDate;
    private final Security security;
    private final TradeType tradeType;
    private final BigDecimal quantity;
    private final BigDecimal grossAmountGbp;
    private final BigDecimal feeGbp;

    public Trade(
            String tradeId,
            LocalDate transactionDate,
            Security security,
            TradeType tradeType,
            BigDecimal quantity,
            BigDecimal grossAmountGbp,
            BigDecimal feeGbp
    ) {
        this.tradeId = Objects.requireNonNull(tradeId, "tradeId");
        this.transactionDate = Objects.requireNonNull(transactionDate, "transactionDate");
        this.security = Objects.requireNonNull(security, "security");
        this.tradeType = Objects.requireNonNull(tradeType, "tradeType");
        this.quantity = Objects.requireNonNull(quantity, "quantity");
        this.grossAmountGbp = Objects.requireNonNull(grossAmountGbp, "grossAmountGbp");
        this.feeGbp = Objects.requireNonNull(feeGbp, "feeGbp");

        if (this.tradeId.isBlank()) {
            throw new IllegalArgumentException("tradeId must not be blank");
        }
        if (this.quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
        if (this.grossAmountGbp.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("grossAmountGbp must not be negative");
        }
        if (this.feeGbp.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("feeGbp must not be negative");
        }
    }

    public String getTradeId() {
        return tradeId;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public Security getSecurity() {
        return security;
    }

    public TradeType getTradeType() {
        return tradeType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getGrossAmountGbp() {
        return grossAmountGbp;
    }

    public BigDecimal getFeeGbp() {
        return feeGbp;
    }

}
