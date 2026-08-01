package com.helper.income.implementation.dividend;

import com.helper.config.CurrencyCode;
import com.helper.config.TaxYear;
import com.helper.config.TaxYearPeriod;
import com.helper.income.implementation.dividend.model.AccumulatingFundReport;
import com.helper.income.implementation.dividend.model.HoldingMovementType;
import com.helper.security.Security;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
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

    public void setMonthlyRate(TaxYearPeriod period, BigDecimal rate) {
        monthlyRates.put(
                Objects.requireNonNull(period, "period"),
                Objects.requireNonNull(rate, "rate")
        );
    }

    public void setYearlyRate(BigDecimal yearlyRate) {
        this.yearlyRate = Objects.requireNonNull(yearlyRate, "yearlyRate");
    }

    /**
     * Adds a buy/sell movement after the opening holding baseline.
     */
    public void addHoldingMovement(
            LocalDate transactionDate,
            BigDecimal quantity,
            HoldingMovementType movementType
    ) {
        LocalDate movementDate = Objects.requireNonNull(transactionDate, "transactionDate");
        BigDecimal movementQuantity = Objects.requireNonNull(quantity, "quantity");
        HoldingMovementType type = Objects.requireNonNull(movementType, "movementType");

        if (movementDate.isBefore(fundReport.getReportingPeriodStartDate())) {
            throw new IllegalArgumentException("transactionDate must not be before fund report period start date");
        }
        if (movementDate.isAfter(fundReport.getReportingPeriodEndDate())) {
            throw new IllegalArgumentException("transactionDate must not be after fund report period end date");
        }

        if (type == HoldingMovementType.BUY) {
            quantityHeld = quantityHeld.add(movementQuantity);
        } else {
            quantityHeld = quantityHeld.subtract(movementQuantity);
        }
    }

    /**
     * Returns the accumulating fund dividend amount in the fund report currency.
     */
    @Override
    public BigDecimal calculateDividendAmount() {
        if (quantityHeld.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("quantity held must not be negative");
        }

        return quantityHeld.multiply(fundReport.getReportedIncomePerUnit());
    }

    public BigDecimal calculateDividendAmountInGbp() {
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

    public BigDecimal calculateDividendAmountInGbpWithMonthlyRate() {
        BigDecimal monthlyRate = getFundDistributionPeriodMonthlyRate();
        if (monthlyRate == null) {
            throw new IllegalStateException("monthly rate must be set for fund distribution period");
        }

        return convertToGbp(calculateDividendAmount(), monthlyRate);
    }

    public BigDecimal calculateDividendAmountInGbpWithYearlyRate() {
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
