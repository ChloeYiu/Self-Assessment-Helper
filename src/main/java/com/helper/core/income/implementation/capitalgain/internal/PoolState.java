package com.helper.core.income.implementation.capitalgain.internal;

import com.helper.core.config.TaxYear;
import com.helper.core.income.implementation.capitalgain.model.CarryForwardSnapshot;
import com.helper.core.income.implementation.capitalgain.model.Match;
import com.helper.core.security.Security;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class PoolState {
    private BigDecimal quantity;
    private BigDecimal costGbp;

    public PoolState(BigDecimal quantity, BigDecimal costGbp) {
        this.quantity = quantity;
        this.costGbp = costGbp;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getCostGbp() {
        return costGbp;
    }

    public void addRemainingBuy(BuyState buyState) {
        this.quantity = this.quantity.add(buyState.getRemainingQuantity());
        this.costGbp = this.costGbp.add(buyState.getRemainingCostGbp());
        buyState.clearRemaining();
    }

    public void addCost(BigDecimal amountGbp) {
        this.costGbp = this.costGbp.add(amountGbp);
    }

    public BigDecimal removeForSell(BigDecimal quantitySold) {
        if (quantitySold.compareTo(quantity) > 0) {
            throw new IllegalStateException("cannot sell more pooled quantity than is available");
        }
        BigDecimal allocatedCost = costGbp
                .multiply(quantitySold)
                .divide(quantity, 10, RoundingMode.HALF_UP);
        quantity = quantity.subtract(quantitySold);
        costGbp = costGbp.subtract(allocatedCost);
        return allocatedCost;
    }

    public CarryForwardSnapshot createCarryForwardSnapshot(
            TaxYear taxYear,
            Security security,
            List<Match> consumedAcquisitions
    ) {
        return new CarryForwardSnapshot(
                taxYear,
                security,
                quantity,
                costGbp,
                consumedAcquisitions
        );
    }
}
