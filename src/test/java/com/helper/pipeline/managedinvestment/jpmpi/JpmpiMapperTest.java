package com.helper.pipeline.managedinvestment.jpmpi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.helper.income.IncomeType;
import com.helper.issue.Issue;
import com.helper.issue.IssueSeverity;
import java.math.BigDecimal;
import org.junit.Test;

public class JpmpiMapperTest {
    @Test
    public void map_readsSummaryPageTotalsAndIgnoresRepeatedScheduleAmounts() {
        JpmpiImportResult result = new JpmpiMapper().map(JpmpiTestDocuments.summaryDocument());

        assertEquals("J.P. Morgan Personal Investing", result.providerName());
        assertEquals("NEWJRFP2", result.accountId());
        assertEquals(2025, result.taxYear().getStartYear());
        assertEquals("jpmpi-2025-26.pdf", result.fileName());
        assertAmount("3.50", result, IncomeType.FOREIGN_SAVINGS);
        assertAmount("3.40", result, IncomeType.FOREIGN_DIVIDENDS);
        assertAmount("4.50", result, IncomeType.UK_SAVINGS);
        assertAmount("-6.70", result, IncomeType.CAPITAL_GAIN);
        assertTrue(result.issues().isEmpty());
    }

    @Test
    public void map_reportsIssueWhenAnIdentifiedPackHasNoReportedTotals() {
        JpmpiImportResult result = new JpmpiMapper().map(JpmpiTestDocuments.noTotalsDocument());

        assertTrue(result.reportedAmounts().isEmpty());
        assertEquals(1, result.issues().size());
        Issue issue = result.issues().getFirst();
        assertEquals(JpmpiIssueCode.SOURCE_TOTALS_UNAVAILABLE, issue.getCode());
        assertEquals(IssueSeverity.ERROR, issue.getSeverity());
        assertEquals("jpmpi-source", issue.getSource());
        assertEquals("empty-jpmpi-2025-26.pdf", issue.getAttributes().get("fileName"));
    }

    private static void assertAmount(String expected, JpmpiImportResult result, IncomeType incomeType) {
        assertEquals(0, new BigDecimal(expected).compareTo(result.reportedAmounts().get(incomeType)));
    }
}
