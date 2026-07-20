package com.helper.core.income.implementation.capitalgain.internal;

import com.helper.core.income.implementation.capitalgain.model.Match;
import com.helper.core.income.implementation.capitalgain.model.Trade;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class BuyState {
    private final Trade trade;
    private BigDecimal remainingQuantity;
    private BigDecimal remainingCostGbp;

    public BuyState(Trade trade) {
        this.trade = trade;
        this.remainingQuantity = trade.getQuantity();
        this.remainingCostGbp = trade.getGrossAmountGbp().add(trade.getFeeGbp());
    }

    public String getTradeId() {
        return trade.getTradeId();
    }

    public BigDecimal getRemainingQuantity() {
        return remainingQuantity;
    }

    public BigDecimal getRemainingCostGbp() {
        return remainingCostGbp;
    }

    public boolean hasRemainingQuantity() {
        return remainingQuantity.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal consume(BigDecimal quantity) {
        BigDecimal cost = remainingCostGbp
                .multiply(quantity)
                .divide(remainingQuantity, 10, RoundingMode.HALF_UP);
        consume(quantity, cost);
        return cost;
    }

    public void consume(Match match) {
        consume(match.getQuantity(), match.getCostGbp());
    }

    private void consume(BigDecimal quantity, BigDecimal costGbp) {
        if (quantity.compareTo(remainingQuantity) > 0) {
            throw new IllegalStateException("cannot consume more buy quantity than is available");
        }
        if (costGbp.compareTo(remainingCostGbp) > 0) {
            throw new IllegalStateException("cannot consume more buy cost than is available");
        }
        remainingQuantity = remainingQuantity.subtract(quantity);
        remainingCostGbp = remainingCostGbp.subtract(costGbp);
    }

    public void clearRemaining() {
        remainingQuantity = BigDecimal.ZERO;
        remainingCostGbp = BigDecimal.ZERO;
    }
}
