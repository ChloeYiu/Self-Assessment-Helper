package com.helper.income.implementation.capitalgain.internal;

import com.helper.income.implementation.capitalgain.model.Match;
import com.helper.income.implementation.capitalgain.model.MatchType;
import com.helper.income.implementation.Trade;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SellState {
    private final Trade trade;
    private BigDecimal remainingQuantity;
    private BigDecimal allowableCostGbp;
    private final List<Match> matches;

    public SellState(Trade trade) {
        this.trade = trade;
        this.remainingQuantity = trade.getQuantity();
        this.allowableCostGbp = BigDecimal.ZERO;
        this.matches = new ArrayList<>();
    }

    public String getTradeId() {
        return trade.getTradeId();
    }

    public LocalDate getTransactionDate() {
        return trade.getTransactionDate();
    }

    public BigDecimal getRemainingQuantity() {
        return remainingQuantity;
    }

    public boolean hasRemainingQuantity() {
        return remainingQuantity.compareTo(BigDecimal.ZERO) > 0;
    }

    public void matchBuys(List<BuyState> buyStates, MatchType matchType) {
        for (BuyState buyState : buyStates) {
            if (!hasRemainingQuantity()) {
                break;
            }
            if (!buyState.hasRemainingQuantity()) {
                continue;
            }
            matchBuy(buyState, matchType);
        }
    }

    public void matchBuy(BuyState buyState, MatchType matchType) {
        BigDecimal matchedQuantity = remainingQuantity.min(buyState.getRemainingQuantity());
        BigDecimal matchedCost = buyState.consume(matchedQuantity);
        remainingQuantity = remainingQuantity.subtract(matchedQuantity);
        allowableCostGbp = allowableCostGbp.add(matchedCost);
        matches.add(new Match(
                buyState.getTradeId(),
                getTradeId(),
                matchedQuantity,
                matchedCost,
                matchType
        ));
    }

    public void matchPool(PoolState pool) {
        if (remainingQuantity.compareTo(pool.getQuantity()) > 0) {
            throw new IllegalStateException("sell trade " + getTradeId() + " has more remaining quantity than the pool can cover");
        }
        BigDecimal pooledCost = pool.removeForSell(remainingQuantity);
        allowableCostGbp = allowableCostGbp.add(pooledCost);
        remainingQuantity = BigDecimal.ZERO;
    }

    public BigDecimal calculateGainGbp() {
        return trade.getGrossAmountGbp()
                .subtract(trade.getFeeGbp())
                .subtract(allowableCostGbp)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public List<Match> getMatches() {
        return List.copyOf(matches);
    }
}
