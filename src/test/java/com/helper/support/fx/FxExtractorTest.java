package com.helper.support.fx;

import com.helper.config.CurrencyCode;
import com.helper.config.TaxYear;
import com.helper.config.TaxYearPeriod;
import com.helper.util.api.MockHttpClient;
import com.helper.util.table.TabularDocument;
import org.junit.Test;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class FxExtractorTest {

    @Test
    public void fetchMonthlyRateDocument_fetchesOneTableForEachTaxYearPeriod() throws Exception {
        MockHttpClient httpClient = new MockHttpClient();
        for (int index = 0; index < TaxYearPeriod.values().length; index++) {
            httpClient.respondWith(200, rateCsv("USD", "1." + index));
        }
        FxExtractor extractor = new FxExtractor(httpClient);

        TabularDocument document = extractor.fetchMonthlyRateDocument(TaxYear.of(2025));

        assertEquals("HMRC monthly FX 2025", document.getSourceName().orElseThrow());
        assertEquals(TaxYearPeriod.values().length, document.getTables().size());
        assertTrue(document.getTable(TaxYearPeriod.APRIL_6_TO_30.name()).isPresent());
        assertTrue(document.getTable(TaxYearPeriod.APRIL_1_TO_5.name()).isPresent());
        assertRequestedUris(
                httpClient.getRequestedUris(),
                "https://www.trade-tariff.service.gov.uk/uk/api/exchange_rates/files/monthly_csv_2025-4.csv",
                "https://www.trade-tariff.service.gov.uk/uk/api/exchange_rates/files/monthly_csv_2026-4.csv");
    }

    @Test
    public void extractMonthlyRates_returnsRequestedCurrencyRateForEveryPeriod() throws Exception {
        MockHttpClient httpClient = new MockHttpClient();
        for (int index = 0; index < TaxYearPeriod.values().length; index++) {
            httpClient.respondWith(200, rateCsv("USD", "1." + index));
        }
        FxExtractor extractor = new FxExtractor(httpClient);
        TabularDocument document = extractor.fetchMonthlyRateDocument(TaxYear.of(2025));

        Map<TaxYearPeriod, BigDecimal> rates = extractor.extractMonthlyRates(document, CurrencyCode.USD);

        assertEquals(TaxYearPeriod.values().length, rates.size());
        assertEquals(0, new BigDecimal("1.0").compareTo(rates.get(TaxYearPeriod.APRIL_6_TO_30)));
        assertEquals(0, new BigDecimal("1.12").compareTo(rates.get(TaxYearPeriod.APRIL_1_TO_5)));
    }

    @Test
    public void fetchYearlyRateDocument_fetchesMarchAverageForTaxYearEndYear() throws Exception {
        MockHttpClient httpClient = new MockHttpClient()
                .respondWith(200, rateCsv("USD", "1.33"));
        FxExtractor extractor = new FxExtractor(httpClient);

        TabularDocument document = extractor.fetchYearlyRateDocument(TaxYear.of(2025));

        assertEquals("HMRC yearly average FX 2025", document.getSourceName().orElseThrow());
        assertEquals(1, document.getTables().size());
        assertEquals(
                URI.create("https://www.trade-tariff.service.gov.uk/uk/api/exchange_rates/files/average_csv_2026-3.csv"),
                httpClient.getRequestedUris().get(0));
    }

    @Test
    public void extractYearlyRate_returnsRequestedCurrencyRateFromYearlyDocument() throws Exception {
        MockHttpClient httpClient = new MockHttpClient()
                .respondWith(200, rateCsv("USD", "1.33"));
        FxExtractor extractor = new FxExtractor(httpClient);
        TabularDocument document = extractor.fetchYearlyRateDocument(TaxYear.of(2025));

        BigDecimal rate = extractor.extractYearlyRate(document, CurrencyCode.USD);

        assertEquals(0, new BigDecimal("1.33").compareTo(rate));
    }


    @Test
    public void extractYearlyRate_throwsWhenCurrencyHasMultipleRows() throws Exception {
        MockHttpClient httpClient = new MockHttpClient()
                .respondWith(200, "Country/Territory,Currency,Currency Code,Currency Units per £1\n"
                        + "United States,Dollar,USD,1.33\n"
                        + "United States,Dollar,USD,1.34\n");
        FxExtractor extractor = new FxExtractor(httpClient);
        TabularDocument document = extractor.fetchYearlyRateDocument(TaxYear.of(2025));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> extractor.extractYearlyRate(document, CurrencyCode.USD));

        assertEquals("multiple FX rates found for USD", exception.getMessage());
    }

    private static String rateCsv(String currencyCode, String rate) {
        return "Country/Territory,Currency,Currency Code,Currency Units per £1\n"
                + "United States,Dollar," + currencyCode + "," + rate + "\n"
                + "Hong Kong,Dollar,HKD,9.80\n";
    }

    private static void assertRequestedUris(List<URI> actualUris, String firstExpected, String lastExpected) {
        assertEquals(URI.create(firstExpected), actualUris.get(0));
        assertEquals(URI.create(lastExpected), actualUris.get(actualUris.size() - 1));
    }
}
