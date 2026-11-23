package com.helper.pipeline.selfdirectedinvestment.consolidation;

import static org.junit.Assert.assertEquals;

import com.helper.config.CurrencyCode;
import com.helper.config.TaxYear;
import com.helper.income.config.CapitalGainCalculationMethod;
import com.helper.income.config.DividendCalculationMethod;
import com.helper.income.config.SecurityConfig;
import com.helper.income.config.SecurityConfigEnums.CapitalGainCalculationMethodType;
import com.helper.income.config.SecurityConfigEnums.DividendCalculationMethodType;
import com.helper.income.config.SecurityConfigEnums.EriReportFormat;
import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.income.implementation.TradeType;
import com.helper.income.implementation.capitalgain.model.CarryForwardSnapshot;
import com.helper.issue.Issue;
import com.helper.issue.IssueCode;
import com.helper.issue.IssueSeverity;
import com.helper.pipeline.selfdirectedinvestment.TaxPipelineInput;
import com.helper.pipeline.selfdirectedinvestment.importer.Position;
import com.helper.pipeline.selfdirectedinvestment.importer.SourceImportResult;
import com.helper.pipeline.selfdirectedinvestment.support.SecuritySupportResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class SourceConsolidatorTest {
    @Test
    public void consolidate_combinesSupportedFactsAndReviewIssuesAcrossSources() {
        Security acwi = security("IE00B44Z5B48", "ACWI");
        Security unsupported = security("IE00UNSUPPORTED", "OTHER");
        Security unimplemented = security("IE00UNIMPLEMENTED", "FUTURE");
        SecurityConfig acwiConfig = config(acwi.getIdentifier());
        PreparedSource firstSource = source(
                List.of(acwi, unsupported, unimplemented),
                List.of(trade("BUY_1", acwi), trade("OTHER_1", unsupported)),
                List.of(position("U123", acwi, "4")),
                acwi,
                acwiConfig,
                List.of(
                        missingConfigIssue(unsupported),
                        unimplementedMethodIssue(unimplemented)));
        PreparedSource secondSource = source(
                List.of(acwi, unsupported, unimplemented),
                List.of(trade("SELL_1", acwi)),
                List.of(position("U456", acwi, "6")),
                acwi,
                acwiConfig,
                List.of(missingConfigIssue(unsupported)));
        TaxPipelineInput input = new TaxPipelineInput(
                TaxYear.of(2025),
                List.of(
                        new CarryForwardSnapshot(TaxYear.of(2025), acwi, new BigDecimal("10"), new BigDecimal("100")),
                        new CarryForwardSnapshot(TaxYear.of(2025), unsupported, new BigDecimal("5"), new BigDecimal("50"))));

        SourceConsolidationResult result = new SourceConsolidator().consolidate(
                List.of(firstSource, secondSource), input);

        assertEquals(1, result.securities().size());
        ConsolidatedSecurityFacts consolidated = result.securities().get(0);
        assertEquals("IE00B44Z5B48", consolidated.security().getIdentifier());
        assertEquals(acwiConfig, consolidated.config());
        assertEquals(List.of("BUY_1", "SELL_1"), consolidated.trades().stream().map(Trade::getTradeId).toList());
        assertEquals(0, new BigDecimal("10").compareTo(consolidated.holdingSnapshot().getQuantity()));
        assertEquals(0, new BigDecimal("10").compareTo(consolidated.carryForwardSnapshot().getQuantity()));
        assertEquals(List.of(
                        IssueCode.MISSING_SECURITY_CONFIGURATION,
                        IssueCode.UNIMPLEMENTED_DIVIDEND_METHOD),
                result.issues().stream().map(Issue::getCode).toList());
    }

    @Test
    public void consolidate_createsZeroSnapshotAndInfoIssueWhenPositionStatementHasNoSecurityRow() {
        Security acwi = security("IE00B44Z5B48", "ACWI");
        SourceConsolidationResult result = new SourceConsolidator().consolidate(
                List.of(source(
                        List.of(acwi),
                        List.of(),
                        List.of(),
                        acwi,
                        config(acwi.getIdentifier()),
                        List.of())),
                new TaxPipelineInput(TaxYear.of(2025), List.of()));

        ConsolidatedSecurityFacts consolidated = result.securities().get(0);
        assertEquals(0, BigDecimal.ZERO.compareTo(consolidated.holdingSnapshot().getQuantity()));
        assertEquals(List.of(IssueCode.ZERO_DIVIDEND_OPENING_HOLDING),
                result.issues().stream().map(Issue::getCode).toList());
    }

    @Test
    public void consolidate_createsZeroSnapshotWhenPositionStatementWasUnavailable() {
        Security acwi = security("IE00B44Z5B48", "ACWI");
        SourceConsolidationResult result = new SourceConsolidator().consolidate(
                List.of(source(
                        List.of(acwi),
                        List.of(),
                        List.of(),
                        acwi,
                        config(acwi.getIdentifier()),
                        List.of())),
                new TaxPipelineInput(TaxYear.of(2025), List.of()));

        assertEquals(0, BigDecimal.ZERO.compareTo(result.securities().get(0)
                .holdingSnapshot().getQuantity()));
    }

    private static PreparedSource source(
            List<Security> securities,
            List<Trade> trades,
            List<Position> positions,
            Security supportedSecurity,
            SecurityConfig config,
            List<Issue> issues
    ) {
        return new PreparedSource(
                new SourceImportResult(
                        securities,
                        trades,
                        positions,
                        LocalDate.of(2024, 10, 6),
                        LocalDate.of(2026, 5, 5),
                        List.of()),
                new SecuritySupportResult(List.of(supportedSecurity), Map.of(
                        supportedSecurity.getIdentifier(), config), issues));
    }

    private static Security security(String isin, String ticker) {
        return new Security(isin, ticker, ticker + " fund", CurrencyCode.USD);
    }

    private static SecurityConfig config(String isin) {
        return new SecurityConfig(
                isin,
                new DividendCalculationMethod(
                        DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND,
                        EriReportFormat.SSGA_SPDR_UK_REPORTABLE_INCOME),
                new CapitalGainCalculationMethod(CapitalGainCalculationMethodType.STANDARD_SECURITY_POOLING));
    }

    private static Trade trade(String id, Security security) {
        return new Trade(
                id,
                LocalDate.of(2025, 4, 7),
                security,
                TradeType.BUY,
                BigDecimal.ONE,
                new BigDecimal("10"),
                BigDecimal.ZERO);
    }

    private static Position position(String accountId, Security security, String quantity) {
        return new Position(
                "ibkr-flex",
                accountId,
                security.getIdentifier(),
                security.getTicker(),
                new BigDecimal(quantity),
                LocalDate.of(2024, 10, 6));
    }

    private static Issue missingConfigIssue(Security security) {
        return new Issue(
                IssueCode.MISSING_SECURITY_CONFIGURATION,
                IssueSeverity.WARNING,
                "security-support",
                Map.of("isin", security.getIdentifier()));
    }

    private static Issue unimplementedMethodIssue(Security security) {
        return new Issue(
                IssueCode.UNIMPLEMENTED_DIVIDEND_METHOD,
                IssueSeverity.WARNING,
                "security-support",
                Map.of("isin", security.getIdentifier()));
    }
}
