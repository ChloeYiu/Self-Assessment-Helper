package com.helper.income.implementation.capitalgain;

import com.helper.config.TaxYear;
import com.helper.income.implementation.capitalgain.internal.BuyState;
import com.helper.income.implementation.capitalgain.internal.PoolState;
import com.helper.income.implementation.capitalgain.internal.SellState;
import com.helper.income.implementation.capitalgain.internal.TradeBookSession;
import com.helper.income.implementation.capitalgain.model.CapitalGainResult;
import com.helper.income.implementation.capitalgain.model.CarryForwardSnapshot;
import com.helper.income.implementation.capitalgain.model.EriAdjustment;
import com.helper.income.implementation.capitalgain.model.Match;
import com.helper.income.implementation.capitalgain.model.MatchType;
import com.helper.income.implementation.capitalgain.model.TradeBook;
import com.helper.income.implementation.Security;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Draft model for UK capital gains on non-UK domiciled accumulating funds.
 *
 * Expected flow:
 * opening pool snapshot + date-indexed trades + required ERI adjustment -> gains/losses + closing pool snapshot.
 * ERI amount may be zero, but callers must provide the adjustment for this tax year.
 */
public class NonUkDomicileAccumulatingCapitalGain implements CapitalGain {
    private final TaxYear taxYear;
    private final Security security;
    private final CarryForwardSnapshot openingCarryForwardSnapshot;
    private final TradeBook tradeBook;
    private final EriAdjustment eriAdjustment;

    public NonUkDomicileAccumulatingCapitalGain(
            TaxYear taxYear,
            Security security,
            CarryForwardSnapshot openingCarryForwardSnapshot,
            EriAdjustment eriAdjustment,
            TradeBook tradeBook
    ) {
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
        this.security = Objects.requireNonNull(security, "security");
        this.openingCarryForwardSnapshot = Objects.requireNonNull(openingCarryForwardSnapshot, "openingCarryForwardSnapshot");
        this.eriAdjustment = Objects.requireNonNull(eriAdjustment, "eriAdjustment");
        this.tradeBook = Objects.requireNonNull(tradeBook, "tradeBook");

        requireSameSecurity(openingCarryForwardSnapshot.getSecurity());
        requireSameSecurity(eriAdjustment.getSecurity());
        requireSameSecurity(tradeBook.getSecurity());
        requireEriAdjustmentInsideTaxYear();
        requireMatchingTradeBookTaxYear();
        requireConsumedAcquisitionsAvailable();
    }

    @Override
    public CapitalGainResult calculateCapitalGain() {
        TradeBookSession session = tradeBook.createSession();
        session.applyConsumedAcquisitions(openingCarryForwardSnapshot.getConsumedAcquisitions());
        PoolState pool = openingCarryForwardSnapshot.createPoolState();
        List<Match> matches = new ArrayList<>();
        BigDecimal totalGain = BigDecimal.ZERO;

        for (LocalDate currentDate : getCalculationDates(session)) {
            if (currentDate.equals(eriAdjustment.getAdjustmentDate())) {
                pool.addCost(eriAdjustment.getAmountGbp());
            }

            // Process sells before pooling same-day buys so same-day matching wins over the pool.
            for (SellState sellState : session.getSellOnDate(currentDate)) {
                matchSell(sellState, pool, session);
                totalGain = totalGain.add(sellState.calculateGainGbp());
                matches.addAll(sellState.getMatches());
            }

            // After same-day and future 30-day matching, remaining buys become part of the pool.
            for (BuyState buyState : session.getBuyOnDate(currentDate)) {
                pool.addRemainingBuy(buyState);
            }
        }

        return new CapitalGainResult(
                totalGain.setScale(2, RoundingMode.HALF_UP),
                pool.createCarryForwardSnapshot(taxYear, security, matches)
        );
    }

    private List<LocalDate> getCalculationDates(TradeBookSession session) {
        NavigableSet<LocalDate> dates = new TreeSet<>();
        if (taxYear.contains(eriAdjustment.getAdjustmentDate())) {
            dates.add(eriAdjustment.getAdjustmentDate());
        }
        session.getSellDates()
                .stream()
                .filter(taxYear::contains)
                .forEach(dates::add);
        session.getBuyDates()
                .stream()
                .filter(taxYear::contains)
                .forEach(dates::add);
        return new ArrayList<>(dates);
    }

    private void matchSell(
            SellState sellState,
            PoolState pool,
            TradeBookSession session
    ) {
        sellState.matchBuys(session.getBuyForMatchType(sellState, MatchType.SAME_DAY), MatchType.SAME_DAY);

        if (sellState.hasRemainingQuantity()) {
            sellState.matchBuys(session.getBuyForMatchType(sellState, MatchType.THIRTY_DAY), MatchType.THIRTY_DAY);
        }

        if (sellState.hasRemainingQuantity()) {
            sellState.matchPool(pool);
        }
    }

    private void requireSameSecurity(Security value) {
        if (!security.getIdentifier().equals(value.getIdentifier())) {
            throw new IllegalArgumentException("security identifier must match capital gain security");
        }
    }

    private void requireMatchingTradeBookTaxYear() {
        if (!taxYear.equals(tradeBook.getTaxYear())) {
            throw new IllegalArgumentException("trade book tax year must match capital gain tax year");
        }
    }

    private void requireEriAdjustmentInsideTaxYear() {
        if (!taxYear.contains(eriAdjustment.getAdjustmentDate())) {
            throw new IllegalArgumentException("ERI adjustment date must be within capital gain tax year");
        }
    }

    private void requireConsumedAcquisitionsAvailable() {
        Map<String, List<Match>> matchesByAcquisitionId = openingCarryForwardSnapshot.getConsumedAcquisitions()
                .stream()
                .collect(Collectors.groupingBy(Match::getAcquisitionTradeId));
        matchesByAcquisitionId.forEach((tradeId, matches) -> tradeBook.requireBuyTradeAvailable(
                tradeId,
                totalQuantity(matches),
                totalCost(matches)
        ));
    }

    private static BigDecimal totalQuantity(List<Match> matches) {
        return matches.stream()
                .map(Match::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal totalCost(List<Match> matches) {
        return matches.stream()
                .map(Match::getCostGbp)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public TaxYear getTaxYear() {
        return taxYear;
    }

    public Security getSecurity() {
        return security;
    }

}
