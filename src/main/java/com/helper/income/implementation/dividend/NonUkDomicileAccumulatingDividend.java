package com.helper.income.implementation.dividend;

import com.helper.config.CurrencyCode;
import com.helper.config.TaxYear;
import com.helper.config.TaxYearPeriod;
import com.helper.income.implementation.dividend.model.AccumulatingFundReport;
import com.helper.income.artifact.HoldingSnapshotArtifact;
import com.helper.income.implementation.dividend.model.DividendResult;
import com.helper.income.implementation.dividend.model.SecurityHoldingSnapshot;
import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.income.implementation.TradeType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Template model for non-UK domicile accumulating dividend income.
 */
public class NonUkDomicileAccumulatingDividend implements Dividend {
    private final TaxYear taxYear;
    private final Security security;
    private final AccumulatingFundReport fundReport;
    private final Map<TaxYearPeriod, BigDecimal> monthlyRates;
    private BigDecimal quantityHeld;
    private BigDecimal yearlyRate;

    public NonUkDomicileAccumulatingDividend(
            TaxYear taxYear,
            Security security,
            AccumulatingFundReport fundReport,
            BigDecimal openingQuantity
    ) {
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
        this.security = Objects.requireNonNull(security, "security");
        this.fundReport = Objects.requireNonNull(fundReport, "fundReport");
        this.quantityHeld = Objects.requireNonNull(openingQuantity, "openingQuantity");
        this.monthlyRates = new EnumMap<>(TaxYearPeriod.class);

        if (!this.taxYear.contains(this.fundReport.getFundDistributionDate())) {
            throw new IllegalArgumentException("fund distribution date must be within the tax year");
        }
        if (!this.security.getIdentifier().equals(this.fundReport.getFundIdentifier())) {
            throw new IllegalArgumentException("security identifier must match fund report identifier");
        }
    }

    public void setMonthlyRates(Map<TaxYearPeriod, BigDecimal> monthlyRates) {
        Objects.requireNonNull(monthlyRates, "monthlyRates")
                .forEach((period, rate) -> this.monthlyRates.put(
                        Objects.requireNonNull(period, "period"),
                        Objects.requireNonNull(rate, "rate")
                ));
    }

    public void setYearlyRate(BigDecimal yearlyRate) {
        this.yearlyRate = Objects.requireNonNull(yearlyRate, "yearlyRate");
    }

    /**
     * Adds trades after the opening holding baseline.
     */
    public void addTrades(List<Trade> trades) {
        Objects.requireNonNull(trades, "trades").forEach(this::addTrade);
    }

    /**
     * Adds one trade after the opening holding baseline.
     */
    public void addTrade(Trade trade) {
        Trade value = Objects.requireNonNull(trade, "trade");
        requireSameSecurity(value);
        requireInsideReportPeriod(value);

        if (value.getTradeType() == TradeType.BUY) {
            quantityHeld = quantityHeld.add(value.getQuantity());
        } else {
            quantityHeld = quantityHeld.subtract(value.getQuantity());
        }
    }

    private void requireSameSecurity(Trade trade) {
        if (!security.getIdentifier().equals(trade.getSecurity().getIdentifier())) {
            throw new IllegalArgumentException("trade security must match dividend security");
        }
    }

    private void requireInsideReportPeriod(Trade trade) {
        LocalDate tradeDate = trade.getTransactionDate();
        if (tradeDate.isBefore(fundReport.getReportingPeriodStartDate())) {
            throw new IllegalArgumentException("trade date must not be before fund report period start date");
        }
        if (tradeDate.isAfter(fundReport.getReportingPeriodEndDate())) {
            throw new IllegalArgumentException("trade date must not be after fund report period end date");
        }
    }

    /**
     * Returns the accumulating fund dividend amount in the fund report currency.
     */
    private BigDecimal calculateDividendAmount() {
        if (quantityHeld.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("quantity held must not be negative");
        }

        return quantityHeld.multiply(fundReport.getReportedIncomePerUnit());
    }

    private BigDecimal calculateDividendAmountInGbp() {
        boolean canUseMonthlyRate = getFundDistributionPeriodMonthlyRate() != null;
        boolean canUseYearlyRate = yearlyRate != null;

        if (canUseMonthlyRate && canUseYearlyRate) {
            return calculateDividendAmountInGbpWithMonthlyRate()
                    .min(calculateDividendAmountInGbpWithYearlyRate());
        }

        if (canUseMonthlyRate) {
            return calculateDividendAmountInGbpWithMonthlyRate();
        }

        if (canUseYearlyRate) {
            return calculateDividendAmountInGbpWithYearlyRate();
        }

        throw new IllegalStateException("monthly rate or yearly rate must be provided");
    }

    /**
     * Returns the GBP dividend result and closing holding snapshot artifact.
     */
    @Override
    public DividendResult calculateDividendResult() {
        BigDecimal dividendAmountGbp = calculateDividendAmountInGbp();
        SecurityHoldingSnapshot closingHoldingSnapshot = new SecurityHoldingSnapshot(
                fundReport.getReportingPeriodEndDate(),
                security,
                quantityHeld
        );

        return new DividendResult(
                dividendAmountGbp,
                List.of(new HoldingSnapshotArtifact(closingHoldingSnapshot))
        );
    }

    private BigDecimal calculateDividendAmountInGbpWithMonthlyRate() {
        BigDecimal monthlyRate = getFundDistributionPeriodMonthlyRate();
        if (monthlyRate == null) {
            throw new IllegalStateException("monthly rate must be set for fund distribution period");
        }

        return convertToGbp(calculateDividendAmount(), monthlyRate);
    }

    private BigDecimal calculateDividendAmountInGbpWithYearlyRate() {
        if (yearlyRate == null) {
            throw new IllegalStateException("yearlyRate must be set before yearly calculation");
        }

        return convertToGbp(calculateDividendAmount(), yearlyRate);
    }

    private BigDecimal getFundDistributionPeriodMonthlyRate() {
        return monthlyRates.get(taxYear.getPeriodForDate(fundReport.getFundDistributionDate()));
    }

    private BigDecimal convertToGbp(BigDecimal amount, BigDecimal rate) {
        return Objects.requireNonNull(amount, "amount")
                .multiply(Objects.requireNonNull(rate, "rate"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public TaxYear getTaxYear() {
        return taxYear;
    }

    public String getFundIdentifier() {
        return security.getIdentifier();
    }

    public String getFundName() {
        return security.getName();
    }

    public String getTicker() {
        return security.getTicker();
    }

    public CurrencyCode getTradingCurrencyCode() {
        return security.getCurrencyCode();
    }

    public CurrencyCode getReportCurrencyCode() {
        return fundReport.getCurrencyCode();
    }
}
