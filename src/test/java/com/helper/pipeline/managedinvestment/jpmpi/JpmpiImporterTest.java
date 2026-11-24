package com.helper.pipeline.managedinvestment.jpmpi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.helper.income.IncomeType;
import com.helper.util.pdf.Pdf;
import java.math.BigDecimal;
import org.junit.Test;

public class JpmpiImporterTest {
    @Test
    public void importSource_extractsAndMapsReportedTotalsFromLocalPdf() throws Exception {
        JpmpiImportResult result = new JpmpiImporter().importSource(
                JpmpiTestDocuments.summaryPdf());

        assertEquals("NEWJRFP2", result.accountId());
        assertEquals(2025, result.taxYear().getStartYear());
        assertAmount("3.50", result, IncomeType.FOREIGN_SAVINGS);
        assertAmount("3.40", result, IncomeType.FOREIGN_DIVIDENDS);
        assertAmount("4.50", result, IncomeType.UK_SAVINGS);
        assertAmount("-6.70", result, IncomeType.CAPITAL_GAIN);
        assertTrue(result.issues().isEmpty());
    }

    private static void assertAmount(String expected, JpmpiImportResult result, IncomeType incomeType) {
        assertEquals(0, new BigDecimal(expected).compareTo(result.reportedAmounts().get(incomeType)));
    }
}
