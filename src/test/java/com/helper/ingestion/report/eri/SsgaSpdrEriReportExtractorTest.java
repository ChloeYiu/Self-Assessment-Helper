package com.helper.ingestion.report.eri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.helper.config.CurrencyCode;
import com.helper.income.implementation.dividend.model.AccumulatingFundReport;
import com.helper.util.api.MockHttpClient;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;
import org.junit.Test;

public class SsgaSpdrEriReportExtractorTest {
    private static final String ACWI_ISIN = "IE00B44Z5B48";

    @Test
    public void extractReport_rejectsYearsOutsideSupportedRangeBeforeFetching() {
        MockHttpClient httpClient = new MockHttpClient();
        SsgaSpdrEriReportExtractor extractor = new SsgaSpdrEriReportExtractor(httpClient);

        IllegalArgumentException beforeRange = assertThrows(IllegalArgumentException.class,
                () -> extractor.extractReport(ACWI_ISIN, 2022));
        IllegalArgumentException afterRange = assertThrows(IllegalArgumentException.class,
                () -> extractor.extractReport(ACWI_ISIN, 2026));

        assertEquals("SSGA Europe I report year is not supported: 2022", beforeRange.getMessage());
        assertEquals("SSGA Europe I report year is not supported: 2026", afterRange.getMessage());
        assertEquals(0, httpClient.getRequestedUris().size());
    }

    @Test
    public void extractReport_readsNewSsgaSpdrReportFormatForAcwi() throws Exception {
        Path pdfPath = resourcePath("ssga-spdr-europe-i-reportable-income-2025.pdf");
        MockHttpClient httpClient = new MockHttpClient()
                .respondWith(200, Files.readAllBytes(pdfPath));

        AccumulatingFundReport report = new SsgaSpdrEriReportExtractor(httpClient)
                .extractReport(ACWI_ISIN, 2025);

        assertReport(report, 2025, "3.4253", CurrencyCode.USD);
        assertEquals(URI.create("https://www.ssga.com/library-content/announcement/etfs/emea/"
                        + "uk-reportable-income/ssga-spdr-etfs-europe-i-plc-reportable-income-report-as-at-31-march-2025.pdf"),
                httpClient.getRequestedUris().get(0));
    }

    @Test
    public void extractReport_readsLegacyRevisedSsgaSpdrReportFormatForAcwi() throws Exception {
        Path pdfPath = resourcePath("revised-ssga-spdr-i-excess-reportable-income-2023.pdf");
        MockHttpClient httpClient = new MockHttpClient()
                .respondWith(200, Files.readAllBytes(pdfPath));

        AccumulatingFundReport report = new SsgaSpdrEriReportExtractor(httpClient)
                .extractReport(ACWI_ISIN, 2023);

        assertReport(report, 2023, "2.7449", CurrencyCode.USD);
        assertEquals(URI.create("https://www.ssga.com/library-content/announcement/etfs/emea/"
                        + "uk-reportable-income/revised-ssga-spdr-i-excess-reportable-income-report-as-at-31-march-2023.pdf"),
                httpClient.getRequestedUris().get(0));
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
