package com.helper.income.implementation.capitalgain.internal;

import com.helper.income.implementation.capitalgain.model.Match;
import com.helper.income.implementation.capitalgain.model.MatchType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class TradeBookSession {
    private final NavigableMap<LocalDate, List<BuyState>> buyStatesWithDate;
    private final NavigableMap<LocalDate, List<SellState>> sellStatesWithDate;

    public TradeBookSession(
            NavigableMap<LocalDate, List<BuyState>> buyStatesWithDate,
            NavigableMap<LocalDate, List<SellState>> sellStatesWithDate
    ) {
        this.buyStatesWithDate = buyStatesWithDate;
        this.sellStatesWithDate = sellStatesWithDate;
    }

    public List<BuyState> getBuyOnDate(LocalDate date) {
        return buyStatesWithDate.getOrDefault(date, List.of())
                .stream()
                .filter(BuyState::hasRemainingQuantity)
                .collect(Collectors.toList());
    }

    public List<BuyState> getBuyWithinThirtyDaysAfter(LocalDate disposalDate) {
        LocalDate startDate = disposalDate.plusDays(1);
        LocalDate endDate = disposalDate.plusDays(30);
        return getBuyBetween(startDate, endDate);
    }

    public List<BuyState> getBuyForMatchType(SellState sellState, MatchType matchType) {
        switch (matchType) {
            case SAME_DAY:
                return getBuyOnDate(sellState.getTransactionDate());
            case THIRTY_DAY:
                return getBuyWithinThirtyDaysAfter(sellState.getTransactionDate());
            default:
                throw new IllegalArgumentException("unsupported match type");
        }
    }

    public List<BuyState> getBuyBetween(LocalDate startDate, LocalDate endDate) {
        return buyStatesWithDate.subMap(startDate, true, endDate, true)
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(BuyState::hasRemainingQuantity)
                .collect(Collectors.toList());
    }

    public List<SellState> getSellOnDate(LocalDate date) {
        return sellStatesWithDate.getOrDefault(date, List.of());
    }

    public List<LocalDate> getBuyDates() {
        return new ArrayList<>(buyStatesWithDate.keySet());
    }

    public List<LocalDate> getSellDates() {
        return new ArrayList<>(sellStatesWithDate.keySet());
    }

    public void applyConsumedAcquisitions(List<Match> consumedAcquisitions) {
        for (Match match : consumedAcquisitions) {
            BuyState buyState = findBuyState(match.getAcquisitionTradeId())
                    .orElseThrow(() -> new IllegalStateException("consumed acquisition is missing from trade book"));
            buyState.consume(match);
        }
    }

    private Optional<BuyState> findBuyState(String tradeId) {
        return buyStatesWithDate.values()
                .stream()
                .flatMap(List::stream)
                .filter(buyState -> buyState.getTradeId().equals(tradeId))
                .findFirst();
    }
}
