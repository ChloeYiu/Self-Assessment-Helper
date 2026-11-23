package com.helper.calculations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.helper.config.TaxYear;
import com.helper.income.IncomeType;
import com.helper.income.implementation.dividend.Dividend;
import com.helper.income.implementation.dividend.model.DividendResult;
import com.helper.issue.Issue;
import com.helper.pipeline.selfdirectedinvestment.dividend.DividendIssueCode;
import com.helper.issue.IssueSeverity;
import com.helper.pipeline.selfdirectedinvestment.SelfDirectedInvestmentPipelineResult;
import com.helper.pipeline.TaxPipeline;
import com.helper.pipeline.TaxPipelineFactory;
import com.helper.pipeline.TaxPipelineResult;
import com.helper.pipeline.TaxPipelineType;
import com.helper.support.fx.FxRateProvider;
import com.helper.support.registry.Registry;
import com.helper.support.registry.RegistryKeys;
import com.helper.util.table.TabularDocument;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class TaxCalculatorTest {
    @Test
    public void run_selectedInvestmentPipelineReturnsDividendResultAndInitializesSharedFxDocuments() throws Exception {
        Dividend dividend = new FixedDividend(TaxYear.of(2025), new BigDecimal("12.34"));
        Issue issue = new Issue(
                DividendIssueCode.UNIMPLEMENTED_METHOD,
                IssueSeverity.WARNING,
                "test",
                Map.of("isin", "IE00B44Z5B48"));
        TaxPipelineFactory investmentPipelineFactory = registry -> new TaxPipeline() {
            @Override
            public TaxPipelineResult run(TaxCalculationRequest request) {
                return new SelfDirectedInvestmentPipelineResult(List.of(), List.of(dividend), List.of(issue));
            }
        };
        Registry registry = new Registry();
        TaxCalculator calculator = new TaxCalculator(
                new FixedFxRateProvider(),
                Map.of(TaxPipelineType.SELF_DIRECTED_INVESTMENT, investmentPipelineFactory));

        TaxCalculationResult result = calculator.run(
                new TaxCalculationRequest(TaxYear.of(2025), Set.of(TaxPipelineType.SELF_DIRECTED_INVESTMENT)),
                registry);

        assertEquals(new BigDecimal("12.34"), result.findIncomeResult(IncomeType.FOREIGN_DIVIDENDS)
                .orElseThrow().getGrossIncome());
        assertEquals(List.of(issue), result.getIssues());
        assertTrue(registry.contains(RegistryKeys.MONTHLY_FX_RATE_DOCUMENT));
        assertTrue(registry.contains(RegistryKeys.YEARLY_FX_RATE_DOCUMENT));
    }

    private static class FixedFxRateProvider extends FxRateProvider {
        @Override
        public TabularDocument fetchMonthlyRateDocument(TaxYear taxYear) {
            return new TabularDocument(List.of());
        }

        @Override
        public TabularDocument fetchYearlyRateDocument(TaxYear taxYear) {
            return new TabularDocument(List.of());
        }
    }

    private record FixedDividend(TaxYear taxYear, BigDecimal amount) implements Dividend {
        @Override
        public DividendResult calculateDividendResult() {
            return new DividendResult(amount);
        }

        @Override
        public TaxYear getTaxYear() {
            return taxYear;
        }
    }
}
