package com.helper.income.implementation.capitalgain.model;

import com.helper.config.TaxYear;
import com.helper.income.implementation.capitalgain.internal.BuyState;
import com.helper.income.implementation.capitalgain.internal.SellState;
import com.helper.income.implementation.capitalgain.internal.TradeBookSession;
import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.income.implementation.TradeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TradeBook {
    private final Security security;
    private final TaxYear taxYear;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final NavigableMap<LocalDate, List<Trade>> buyTradesByDate;
    private final NavigableMap<LocalDate, List<Trade>> sellTradesByDate;

    public TradeBook(Security security, TaxYear taxYear) {
        this.security = Objects.requireNonNull(security, "security");
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
        this.startDate = taxYear.getStartDate();
        this.endDate = taxYear.getEndDate().plusDays(30);
        this.buyTradesByDate = new TreeMap<>();
        this.sellTradesByDate = new TreeMap<>();
    }

    public void add(Trade trade) {
        Trade value = Objects.requireNonNull(trade, "trade");
        requireSameSecurity(value.getSecurity());
        requireInsidePeriod(value.getTransactionDate());
        if (value.getTradeType() == TradeType.BUY) {
            addTradeByDate(buyTradesByDate, value);
        } else {
            addTradeByDate(sellTradesByDate, value);
        }
    }

    public Security getSecurity() {
        return security;
    }

    public TaxYear getTaxYear() {
        return taxYear;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public TradeBookSession createSession() {
        NavigableMap<LocalDate, List<BuyState>> buyStatesWithDate = createBuyStatesWithDate();
        NavigableMap<LocalDate, List<SellState>> sellStatesWithDate = createSellStatesWithDate();
        return new TradeBookSession(buyStatesWithDate, sellStatesWithDate);
    }

    public void requireBuyTradeAvailable(String tradeId, BigDecimal quantity, BigDecimal costGbp) {
        Trade trade = findBuyTrade(tradeId);
        if (trade == null) {
            throw new IllegalArgumentException("buy trade is missing from trade book");
        }
        if (quantity.compareTo(trade.getQuantity()) > 0) {
            throw new IllegalArgumentException("required buy quantity exceeds trade quantity");
        }
        if (costGbp.compareTo(trade.getGrossAmountGbp().add(trade.getFeeGbp())) > 0) {
            throw new IllegalArgumentException("required buy cost exceeds trade cost");
        }
    }

    private NavigableMap<LocalDate, List<BuyState>> createBuyStatesWithDate() {
        NavigableMap<LocalDate, List<BuyState>> buyStatesWithDate = new TreeMap<>();
        buyTradesByDate.forEach((date, trades) -> trades.stream()
                .sorted(Comparator.comparing(Trade::getTradeId))
                .forEach(trade -> buyStatesWithDate
                        .computeIfAbsent(date, ignored -> new ArrayList<>())
                        .add(new BuyState(trade))));
        return buyStatesWithDate;
    }

    private NavigableMap<LocalDate, List<SellState>> createSellStatesWithDate() {
        NavigableMap<LocalDate, List<SellState>> sellStatesWithDate = new TreeMap<>();
        sellTradesByDate.forEach((date, trades) -> sellStatesWithDate.put(date, trades.stream()
                .sorted(Comparator.comparing(Trade::getTradeId))
                .map(SellState::new)
                .collect(Collectors.toList())));
        return sellStatesWithDate;
    }

    private void requireSameSecurity(Security value) {
        if (!security.getIdentifier().equals(value.getIdentifier())) {
            throw new IllegalArgumentException("trade security must match trade book security");
        }
    }

    private void requireInsidePeriod(LocalDate date) {
        if (date.isBefore(startDate) || date.isAfter(endDate)) {
            throw new IllegalArgumentException("trade date is outside trade book period");
        }
    }

    private static void addTradeByDate(
            NavigableMap<LocalDate, List<Trade>> tradesByDate,
            Trade trade
    ) {
        tradesByDate
                .computeIfAbsent(trade.getTransactionDate(), ignored -> new ArrayList<>())
                .add(trade);
    }

    private Trade findBuyTrade(String tradeId) {
        return buyTradesByDate.values()
                .stream()
                .flatMap(List::stream)
                .filter(trade -> trade.getTradeId().equals(tradeId))
                .findFirst()
                .orElse(null);
    }
}
