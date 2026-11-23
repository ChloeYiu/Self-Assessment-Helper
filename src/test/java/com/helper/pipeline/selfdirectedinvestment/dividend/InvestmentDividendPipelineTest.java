package com.helper.pipeline.selfdirectedinvestment.dividend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.helper.config.CurrencyCode;
import com.helper.config.TaxYear;
import com.helper.income.config.CapitalGainCalculationMethod;
import com.helper.income.config.DividendCalculationMethod;
import com.helper.income.config.SecurityConfig;
import com.helper.income.config.SecurityConfigEnums.CapitalGainCalculationMethodType;
import com.helper.income.config.SecurityConfigEnums.DividendCalculationMethodType;
import com.helper.income.config.SecurityConfigEnums.EriReportFormat;
import com.helper.income.implementation.Security;
import com.helper.income.implementation.dividend.Dividend;
import com.helper.issue.Issue;
import com.helper.issue.IssueCode;
import com.helper.issue.IssueSeverity;
import com.helper.pipeline.selfdirectedinvestment.consolidation.ConsolidatedSecurityFacts;
import com.helper.pipeline.selfdirectedinvestment.consolidation.SourceConsolidationResult;
import com.helper.support.registry.Registry;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class InvestmentDividendPipelineTest {
    @Test
    public void prepare_returnsEmptyResultWhenThereAreNoConsolidatedSecurities() {
        InvestmentDividendResult result = new InvestmentDividendPipeline(new Registry()).prepare(
                new SourceConsolidationResult(TaxYear.of(2025), List.of(), List.of()));

        assertTrue(result.dividendSources().isEmpty());
        assertTrue(result.issues().isEmpty());
    }

    @Test
    public void prepare_mergesDividendSourcesAndIssuesReturnedBySelectedMethodPipeline() {
        TaxYear taxYear = TaxYear.of(2025);
        Registry registry = new Registry();
        ConsolidatedSecurityFacts acwi = facts(
                "IE00B44Z5B48",
                DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND);
        Dividend dividend = mock(Dividend.class);
        Issue issue = new Issue(
                IssueCode.UNIMPLEMENTED_DIVIDEND_METHOD,
                IssueSeverity.WARNING,
                "non-uk-accumulating-fund",
                Map.of("isin", "IE00B44Z5B48"));
        DividendMethodPipeline methodPipeline = methodPipelineReturning(
                taxYear,
                registry,
                List.of(acwi),
                new DividendMethodResult(List.of(dividend), List.of(issue)));
        InvestmentDividendPipeline pipeline = new InvestmentDividendPipeline(
                new DividendMethodDispatcher(Map.of(
                        DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND,
                        methodPipeline)),
                registry);

        InvestmentDividendResult result = pipeline.prepare(new SourceConsolidationResult(
                taxYear, List.of(acwi), List.of()));

        verify(methodPipeline).prepareDividendSources(taxYear, registry, List.of(acwi));
        assertEquals(1, result.dividendSources().size());
        assertSame(dividend, result.dividendSources().get(0));
        assertEquals(List.of(issue), result.issues());
    }

    @Test
    public void prepare_recordsIssueWhenNoPipelineHandlesConfiguredMethod() {
        ConsolidatedSecurityFacts unsupportedSecurity = facts(
                "IE00UNSUPPORTED",
                DividendCalculationMethodType.UNSUPPORTED);

        InvestmentDividendResult result = new InvestmentDividendPipeline(new Registry()).prepare(
                new SourceConsolidationResult(
                        TaxYear.of(2025),
                        List.of(unsupportedSecurity),
                        List.of()));

        assertTrue(result.dividendSources().isEmpty());
        assertEquals(1, result.issues().size());
        Issue issue = result.issues().get(0);
        assertEquals(IssueCode.UNIMPLEMENTED_DIVIDEND_METHOD, issue.getCode());
        assertEquals(IssueSeverity.WARNING, issue.getSeverity());
        assertEquals("investment-dividend", issue.getSource());
        assertEquals("IE00UNSUPPORTED", issue.getAttributes().get("isin"));
        assertEquals("UNSUPPORTED", issue.getAttributes().get("methodType"));
    }

    private static ConsolidatedSecurityFacts facts(
            String isin,
            DividendCalculationMethodType methodType
    ) {
        Security security = new Security(isin, "FUTURE", "Future fund", CurrencyCode.USD);
        EriReportFormat reportFormat = methodType == DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND
                ? EriReportFormat.SSGA_SPDR_UK_REPORTABLE_INCOME
                : null;
        SecurityConfig config = new SecurityConfig(
                isin,
                new DividendCalculationMethod(methodType, reportFormat),
                new CapitalGainCalculationMethod(CapitalGainCalculationMethodType.STANDARD_SECURITY_POOLING));
        return new ConsolidatedSecurityFacts(security, config, List.of(), null, null);
    }

    private static DividendMethodPipeline methodPipelineReturning(
            TaxYear taxYear,
            Registry registry,
            List<ConsolidatedSecurityFacts> securityFacts,
            DividendMethodResult result
    ) {
        DividendMethodPipeline pipeline = mock(DividendMethodPipeline.class);
        when(pipeline.prepareDividendSources(taxYear, registry, securityFacts)).thenReturn(result);
        return pipeline;
    }
}
