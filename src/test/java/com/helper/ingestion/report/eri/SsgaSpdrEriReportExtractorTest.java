package com.helper.ingestion.report.eri;

import static org.junit.Assert.assertEquals;

import com.helper.config.CurrencyCode;
import com.helper.income.implementation.dividend.model.AccumulatingFundReport;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;
import org.junit.Test;

public class SsgaSpdrEriReportExtractorTest {
    private static final String ACWI_ISIN = "IE00B44Z5B48";

    @Test
    public void extractReport_readsNewSsgaSpdrReportFormatForAcwi() throws Exception {
        Path pdfPath = resourcePath("ssga-spdr-europe-i-reportable-income-2025.pdf");

        AccumulatingFundReport report = new SsgaSpdrEriReportExtractor().extractReport(new EriReportInput(
                ACWI_ISIN,
                2025,
                pdfPath,
                null
        ));

        assertReport(report, 2025, "3.4253", CurrencyCode.USD);
    }

    @Test
    public void extractReport_readsLegacyRevisedSsgaSpdrReportFormatForAcwi() throws Exception {
        Path pdfPath = resourcePath("revised-ssga-spdr-i-excess-reportable-income-2023.pdf");

        AccumulatingFundReport report = new SsgaSpdrEriReportExtractor().extractReport(new EriReportInput(
                ACWI_ISIN,
                2023,
                pdfPath,
                null
        ));

        assertReport(report, 2023, "2.7449", CurrencyCode.USD);
    }

    private static Path resourcePath(String fileName) throws Exception {
        URL resource = SsgaSpdrEriReportExtractorTest.class.getResource(fileName);
        return Path.of(Objects.requireNonNull(resource, fileName).toURI());
    }

    private static void assertReport(
            AccumulatingFundReport report,
            int reportingYear,
            String reportedIncomePerUnit,
            CurrencyCode currencyCode
    ) {
        assertEquals(ACWI_ISIN, report.getFundIdentifier());
        assertEquals(LocalDate.of(reportingYear - 1, 4, 1), report.getReportingPeriodStartDate());
        assertEquals(LocalDate.of(reportingYear, 3, 31), report.getReportingPeriodEndDate());
        assertEquals(LocalDate.of(reportingYear, 9, 30), report.getFundDistributionDate());
        assertBigDecimalEquals(reportedIncomePerUnit, report.getReportedIncomePerUnit());
        assertEquals(currencyCode, report.getCurrencyCode());
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
