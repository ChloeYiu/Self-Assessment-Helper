package com.helper.pipeline.managedinvestment;

import com.helper.config.TaxYear;
import com.helper.income.IncomeType;
import com.helper.issue.Issue;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Provider-neutral reported tax facts from one managed-investment source. */
public interface ManagedInvestmentSourceResult {
    /** Returns the tax year stated by the provider source. */
    TaxYear taxYear();

    /** Returns final provider-reported GBP amounts by filing category. */
    Map<IncomeType, BigDecimal> reportedAmounts();

    /** Returns recoverable issues produced while importing that source. */
    List<Issue> issues();
}
