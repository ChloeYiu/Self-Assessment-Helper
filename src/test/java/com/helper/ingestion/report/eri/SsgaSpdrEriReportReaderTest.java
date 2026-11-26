package com.helper.ingestion.report.eri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.helper.config.CurrencyCode;
import com.helper.income.implementation.dividend.model.AccumulatingFundReport;
import com.helper.util.api.MockHttpClient;
import com.helper.util.pdf.Pdf;
import java.math.BigDecimal;
import java.net.URI;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Objects;
import org.junit.Test;

public class SsgaSpdrEriReportReaderTest {
    private static final String ACWI_ISIN = "IE00B44Z5B48";

    @Test
    public void readReport_rejectsYearsOutsideSupportedRangeBeforeFetching() {
        MockHttpClient httpClient = new MockHttpClient();
        EriReportReader extractor = ssgaReader(httpClient);

        IllegalArgumentException beforeRange = assertThrows(IllegalArgumentException.class,
                () -> extractor.readReport(ACWI_ISIN, 2022));
        IllegalArgumentException afterRange = assertThrows(IllegalArgumentException.class,
                () -> extractor.readReport(ACWI_ISIN, 2026));

        assertEquals("SSGA Europe I report year is not supported: 2022", beforeRange.getMessage());
        assertEquals("SSGA Europe I report year is not supported: 2026", afterRange.getMessage());
        assertEquals(0, httpClient.getRequestedUris().size());
    }

    @Test
    public void readReport_readsNewSsgaSpdrReportFormatForAcwi() throws Exception {
        MockHttpClient httpClient = new MockHttpClient()
                .respondWith(200, resourceBytes("ssga-spdr-europe-i-reportable-income-2025.pdf"));

        AccumulatingFundReport report = ssgaReader(httpClient)
                .readReport(ACWI_ISIN, 2025);

        assertReport(report, 2025, "3.4253", CurrencyCode.USD);
        assertEquals(URI.create("https://www.ssga.com/library-content/announcement/etfs/emea/"
                        + "uk-reportable-income/ssga-spdr-etfs-europe-i-plc-reportable-income-report-as-at-31-march-2025.pdf"),
                httpClient.getRequestedUris().get(0));
    }

    @Test
    public void readReport_readsLegacyRevisedSsgaSpdrReportFormatForAcwi() throws Exception {
        MockHttpClient httpClient = new MockHttpClient()
                .respondWith(200, resourceBytes("revised-ssga-spdr-i-excess-reportable-income-2023.pdf"));

        AccumulatingFundReport report = ssgaReader(httpClient)
                .readReport(ACWI_ISIN, 2023);

        assertReport(report, 2023, "2.7449", CurrencyCode.USD);
        assertEquals(URI.create("https://www.ssga.com/library-content/announcement/etfs/emea/"
                        + "uk-reportable-income/revised-ssga-spdr-i-excess-reportable-income-report-as-at-31-march-2023.pdf"),
                httpClient.getRequestedUris().get(0));
    }

    private static byte[] resourceBytes(String fileName) throws Exception {
        try (InputStream resource = SsgaSpdrEriReportReaderTest.class.getResourceAsStream(fileName)) {
            return Objects.requireNonNull(resource, fileName).readAllBytes();
        }
    }

    private static EriReportReader ssgaReader(MockHttpClient httpClient) {
        return new EriReportReader(
                reportingYear -> new Pdf(
                        new SsgaSpdrEriReportProvider().resolveReportUri(reportingYear), httpClient),
                new SsgaSpdrEriReportExtractor());
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
