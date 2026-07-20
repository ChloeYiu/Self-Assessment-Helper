package com.helper.core.income.implementation.dividend.model;

import com.helper.core.config.CurrencyCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Reported income details for a non-UK domiciled accumulating fund.
 */
public class AccumulatingFundReport {
    private final String fundIdentifier;
    private final LocalDate reportingPeriodStartDate;
    private final LocalDate reportingPeriodEndDate;
    private final LocalDate fundDistributionDate;
    private final BigDecimal reportedIncomePerUnit;
    private final CurrencyCode currencyCode;

    public AccumulatingFundReport(
            String fundIdentifier,
            LocalDate reportingPeriodStartDate,
            LocalDate reportingPeriodEndDate,
            LocalDate fundDistributionDate,
            BigDecimal reportedIncomePerUnit,
            CurrencyCode currencyCode
    ) {
        this.fundIdentifier = Objects.requireNonNull(fundIdentifier, "fundIdentifier");
        this.reportingPeriodStartDate = Objects.requireNonNull(
                reportingPeriodStartDate,
                "reportingPeriodStartDate"
        );
        this.reportingPeriodEndDate = Objects.requireNonNull(reportingPeriodEndDate, "reportingPeriodEndDate");
        this.fundDistributionDate = Objects.requireNonNull(fundDistributionDate, "fundDistributionDate");
        this.reportedIncomePerUnit = Objects.requireNonNull(reportedIncomePerUnit, "reportedIncomePerUnit");
        this.currencyCode = Objects.requireNonNull(currencyCode, "currencyCode");

        if (reportingPeriodEndDate.isBefore(reportingPeriodStartDate)) {
            throw new IllegalArgumentException("reportingPeriodEndDate must not be before reportingPeriodStartDate");
        }
    }

    public String getFundIdentifier() {
        return fundIdentifier;
    }

    public LocalDate getReportingPeriodStartDate() {
        return reportingPeriodStartDate;
    }

    public LocalDate getReportingPeriodEndDate() {
        return reportingPeriodEndDate;
    }

    public LocalDate getFundDistributionDate() {
        return fundDistributionDate;
    }

    public BigDecimal getReportedIncomePerUnit() {
        return reportedIncomePerUnit;
    }

    public CurrencyCode getCurrencyCode() {
        return currencyCode;
    }
}
