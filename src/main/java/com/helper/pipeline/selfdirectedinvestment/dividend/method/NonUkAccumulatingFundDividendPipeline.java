package com.helper.pipeline.selfdirectedinvestment.dividend.method;

import com.helper.config.TaxYear;
import com.helper.config.TaxYearPeriod;
import com.helper.income.config.SecurityConfigEnums.DividendCalculationMethodType;
import com.helper.income.config.SecurityConfigEnums.EriReportFormat;
import com.helper.income.implementation.Trade;
import com.helper.income.implementation.dividend.Dividend;
import com.helper.income.implementation.dividend.NonUkDomicileAccumulatingDividend;
import com.helper.income.implementation.dividend.model.AccumulatingFundReport;
import com.helper.income.implementation.dividend.model.SecurityHoldingSnapshot;
import com.helper.ingestion.report.eri.EriReportReader;
import com.helper.ingestion.report.eri.EriReportReaderDispatcher;
import com.helper.issue.Issue;
import com.helper.ingestion.report.eri.EriIssueCode;
import com.helper.issue.IssueCode;
import com.helper.pipeline.selfdirectedinvestment.dividend.DividendIssueCode;
import com.helper.issue.IssueSeverity;
import com.helper.pipeline.selfdirectedinvestment.consolidation.ConsolidatedSecurityFacts;
import com.helper.pipeline.selfdirectedinvestment.dividend.DividendMethodPipeline;
import com.helper.pipeline.selfdirectedinvestment.dividend.DividendMethodResult;
import com.helper.support.fx.FxRateExtractor;
import com.helper.support.registry.Registry;
import com.helper.support.registry.RegistryKeys;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Prepares dividend sources for non-UK domiciled accumulating funds.
 *
 * <p>The completed pipeline will own ERI report loading and create dividend sources from
 * consolidated security facts, opening holding state, trades, and prefetched FX data.</p>
 */
public class NonUkAccumulatingFundDividendPipeline implements DividendMethodPipeline {
    private static final String ISSUE_SOURCE = "non-uk-accumulating-fund-dividend";

    private final EriReportReaderDispatcher reportReaderDispatcher;
    private final FxRateExtractor fxRateExtractor;

    /** Creates the pipeline with the application's ERI extractor and FX-rate routing. */
    public NonUkAccumulatingFundDividendPipeline() {
        this(new EriReportReaderDispatcher(), new FxRateExtractor());
    }

    NonUkAccumulatingFundDividendPipeline(
            EriReportReaderDispatcher reportReaderDispatcher,
            FxRateExtractor fxRateExtractor
    ) {
        this.reportReaderDispatcher = Objects.requireNonNull(reportReaderDispatcher, "reportReaderDispatcher");
        this.fxRateExtractor = Objects.requireNonNull(fxRateExtractor, "fxRateExtractor");
    }

    @Override
    public DividendCalculationMethodType getMethodType() {
        return DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND;
    }

    @Override
    public DividendMethodResult prepareDividendSources(
            TaxYear taxYear,
            Registry registry,
            List<ConsolidatedSecurityFacts> securityFacts
    ) {
        Objects.requireNonNull(taxYear, "taxYear");
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(securityFacts, "securityFacts");
        List<Dividend> dividendSources = new ArrayList<>();
        List<Issue> issues = new ArrayList<>();

        for (ConsolidatedSecurityFacts securityFact : securityFacts) {
            prepareDividendForSecurity(taxYear, registry, securityFact, dividendSources, issues);
        }

        return new DividendMethodResult(dividendSources, issues);
    }

    private void prepareDividendForSecurity(
            TaxYear taxYear,
            Registry registry,
            ConsolidatedSecurityFacts securityFacts,
            List<Dividend> dividendSources,
            List<Issue> issues
    ) {
        Objects.requireNonNull(securityFacts, "securityFacts");
        Optional<AccumulatingFundReport> report = readReport(taxYear, securityFacts, issues);
        Optional<SecurityHoldingSnapshot> snapshot = getOpeningHoldingSnapshot(taxYear, securityFacts, issues);
        if (report.isEmpty() || snapshot.isEmpty()) {
            return;
        }

        Optional<NonUkDomicileAccumulatingDividend> dividend = createDividendSource(
                taxYear, securityFacts, report.get(), snapshot.get(), issues);
        if (dividend.isEmpty()) {
            return;
        }

        if (!addReportPeriodTrades(
                dividend.get(), securityFacts.trades(), taxYear, report.get(), securityFacts, issues)) {
            return;
        }
        if (!applyFxRates(dividend.get(), registry, report.get(), securityFacts, issues)) {
            return;
        }
        if (validateDividendSource(dividend.get(), securityFacts, issues)) {
            dividendSources.add(dividend.get());
        }
    }

    private Optional<AccumulatingFundReport> readReport(
            TaxYear taxYear,
            ConsolidatedSecurityFacts securityFacts,
            List<Issue> issues
    ) {
        EriReportFormat reportFormat = securityFacts.config()
                .getDividendCalculationMethod()
                .getEriReportFormat();
        Optional<EriReportReader> reportReader = reportReaderDispatcher.findReader(reportFormat);
        if (reportReader.isEmpty()) {
            issues.add(issue(EriIssueCode.UNSUPPORTED_REPORT_FORMAT, securityFacts, Map.of(
                    "eriReportFormat", reportFormat.name())));
            return Optional.empty();
        }

        try {
            return Optional.of(reportReader.get().readReport(
                    securityFacts.security().getIdentifier(), taxYear.getStartYear()));
        } catch (IOException | IllegalArgumentException exception) {
            issues.add(issue(EriIssueCode.REPORT_LOAD_FAILED, securityFacts, Map.of(
                    "reportingYear", String.valueOf(taxYear.getStartYear()),
                    "exceptionType", exception.getClass().getSimpleName())));
            return Optional.empty();
        }
    }

    private Optional<SecurityHoldingSnapshot> getOpeningHoldingSnapshot(
            TaxYear taxYear,
            ConsolidatedSecurityFacts securityFacts,
            List<Issue> issues
    ) {
        LocalDate boundaryDate = dividendBoundary(taxYear);
        SecurityHoldingSnapshot snapshot = securityFacts.holdingSnapshot();
        if (snapshot == null || !snapshot.getSnapshotDate().equals(boundaryDate)) {
            issues.add(issue(DividendIssueCode.MISSING_OPENING_HOLDING, securityFacts, Map.of(
                    "boundaryDate", boundaryDate.toString())));
            return Optional.empty();
        }
        return Optional.of(snapshot);
    }

    private Optional<NonUkDomicileAccumulatingDividend> createDividendSource(
            TaxYear taxYear,
            ConsolidatedSecurityFacts securityFacts,
            AccumulatingFundReport report,
            SecurityHoldingSnapshot openingSnapshot,
            List<Issue> issues
    ) {
        try {
            return Optional.of(new NonUkDomicileAccumulatingDividend(
                    taxYear,
                    securityFacts.security(),
                    report,
                    openingSnapshot.getQuantity()));
        } catch (IllegalArgumentException exception) {
            issues.add(preparationFailureIssue(securityFacts, exception));
            return Optional.empty();
        }
    }

    private boolean addReportPeriodTrades(
            NonUkDomicileAccumulatingDividend dividend,
            List<Trade> trades,
            TaxYear taxYear,
            AccumulatingFundReport report,
            ConsolidatedSecurityFacts securityFacts,
            List<Issue> issues
    ) {
        try {
            dividend.addTrades(tradesAfterBoundaryThroughReportEnd(
                    trades, dividendBoundary(taxYear), report));
            return true;
        } catch (IllegalArgumentException exception) {
            issues.add(preparationFailureIssue(securityFacts, exception));
            return false;
        }
    }

    private boolean applyFxRates(
            NonUkDomicileAccumulatingDividend dividend,
            Registry registry,
            AccumulatingFundReport report,
            ConsolidatedSecurityFacts securityFacts,
            List<Issue> issues
    ) {
        Map<TaxYearPeriod, BigDecimal> monthlyRates = extractMonthlyRates(registry, report);
        BigDecimal yearlyRate = extractYearlyRate(registry, report);
        if (monthlyRates == null && yearlyRate == null) {
            issues.add(issue(DividendIssueCode.FX_RATE_UNAVAILABLE, securityFacts, Map.of(
                    "currencyCode", report.getCurrencyCode().name())));
            return false;
        }
        if (monthlyRates != null) {
            dividend.setMonthlyRates(monthlyRates);
        }
        if (yearlyRate != null) {
            dividend.setYearlyRate(yearlyRate);
        }
        return true;
    }

    private Map<TaxYearPeriod, BigDecimal> extractMonthlyRates(
            Registry registry,
            AccumulatingFundReport report
    ) {
        try {
            return fxRateExtractor.extractMonthlyRates(
                    registry.require(RegistryKeys.MONTHLY_FX_RATE_DOCUMENT), report.getCurrencyCode());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private BigDecimal extractYearlyRate(Registry registry, AccumulatingFundReport report) {
        try {
            return fxRateExtractor.extractYearlyRate(
                    registry.require(RegistryKeys.YEARLY_FX_RATE_DOCUMENT), report.getCurrencyCode());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private boolean validateDividendSource(
            NonUkDomicileAccumulatingDividend dividend,
            ConsolidatedSecurityFacts securityFacts,
            List<Issue> issues
    ) {
        try {
            dividend.calculateDividendResult();
            return true;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            issues.add(preparationFailureIssue(securityFacts, exception));
            return false;
        }
    }

    private Issue preparationFailureIssue(
            ConsolidatedSecurityFacts securityFacts,
            RuntimeException exception
    ) {
        return issue(DividendIssueCode.PREPARATION_FAILED, securityFacts, Map.of(
                "exceptionType", exception.getClass().getSimpleName()));
    }

    private static LocalDate dividendBoundary(TaxYear taxYear) {
        return taxYear.getStartDate().minusMonths(6);
    }

    private static List<Trade> tradesAfterBoundaryThroughReportEnd(
            List<Trade> trades,
            LocalDate boundaryDate,
            AccumulatingFundReport report
    ) {
        return trades.stream()
                .filter(trade -> trade.getTransactionDate().isAfter(boundaryDate))
                .filter(trade -> !trade.getTransactionDate().isAfter(report.getReportingPeriodEndDate()))
                .toList();
    }

    private Issue issue(
            IssueCode code,
            ConsolidatedSecurityFacts securityFacts,
            Map<String, String> details
    ) {
        Map<String, String> attributes = new HashMap<>(details);
        attributes.put("isin", securityFacts.security().getIdentifier());
        return new Issue(code, IssueSeverity.ERROR, ISSUE_SOURCE, attributes);
    }
}
