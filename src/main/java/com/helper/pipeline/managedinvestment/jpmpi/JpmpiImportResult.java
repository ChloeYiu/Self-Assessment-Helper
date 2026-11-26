package com.helper.pipeline.managedinvestment.jpmpi;

import com.helper.config.TaxYear;
import com.helper.income.IncomeType;
import com.helper.issue.Issue;
import com.helper.pipeline.managedinvestment.ManagedInvestmentSourceResult;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Provider-reported totals from one J.P. Morgan Personal Investing tax-pack PDF.
 *
 * <p>The totals map includes the four income categories and {@link IncomeType#CAPITAL_GAIN}.
 * The extractor keeps detailed PDF locations while validating the pack; they are not part of
 * this pipeline result.</p>
 */
public final class JpmpiImportResult implements ManagedInvestmentSourceResult {
    private final String providerName;
    private final String accountId;
    private final TaxYear taxYear;
    private final String fileName;
    private final Map<IncomeType, BigDecimal> reportedAmounts = new EnumMap<>(IncomeType.class);
    private final List<Issue> issues;

    /** Creates the result container for one identified JPMPI tax pack. */
    public JpmpiImportResult(
            String providerName,
            String accountId,
            TaxYear taxYear,
            String fileName,
            List<Issue> issues) {
        this.providerName = providerName;
        this.accountId = accountId;
        this.taxYear = taxYear;
        this.fileName = fileName;
        this.issues = issues;
    }

    /** Adds a provider-reported GBP amount to its filing category. */
    public void addReportedAmount(IncomeType incomeType, BigDecimal amount) {
        reportedAmounts.merge(incomeType, amount, BigDecimal::add);
    }

    public String providerName() {
        return providerName;
    }

    public String accountId() {
        return accountId;
    }

    @Override
    public TaxYear taxYear() {
        return taxYear;
    }

    public String fileName() {
        return fileName;
    }

    /** Returns the provider-reported GBP totals by filing category. */
    @Override
    public Map<IncomeType, BigDecimal> reportedAmounts() {
        return Collections.unmodifiableMap(reportedAmounts);
    }

    @Override
    public List<Issue> issues() {
        return issues;
    }
}
