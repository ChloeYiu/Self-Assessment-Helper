package com.helper.support.fx;

import com.helper.config.CurrencyCode;
import com.helper.config.TaxYearPeriod;
import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularTable;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class FxRateExtractorTest {

    @Test
    public void extractMonthlyRates_returnsRequestedCurrencyRateForEveryPeriod() {
        TabularDocument document = monthlyRateDocument();

        Map<TaxYearPeriod, BigDecimal> rates = new FxRateExtractor().extractMonthlyRates(document, CurrencyCode.USD);

        assertEquals(TaxYearPeriod.values().length, rates.size());
        assertEquals(0, new BigDecimal("1.0").compareTo(rates.get(TaxYearPeriod.APRIL_6_TO_30)));
        assertEquals(0, new BigDecimal("1.12").compareTo(rates.get(TaxYearPeriod.APRIL_1_TO_5)));
    }

    @Test
    public void extractYearlyRate_returnsRequestedCurrencyRateFromYearlyDocument() {
        TabularDocument document = yearlyRateDocument(rateTable("YEARLY_AVERAGE", "USD", "1.33"));

        BigDecimal rate = new FxRateExtractor().extractYearlyRate(document, CurrencyCode.USD);

        assertEquals(0, new BigDecimal("1.33").compareTo(rate));
    }

    @Test
    public void extractYearlyRate_throwsWhenCurrencyHasMultipleRows() {
        TabularDocument document = yearlyRateDocument(new com.helper.util.api.CsvDocumentParser().parse(
                "Country/Territory,Currency,Currency Code,Currency Units per £1\n"
                        + "United States,Dollar,USD,1.33\n"
                        + "United States,Dollar,USD,1.34\n",
                "YEARLY_AVERAGE"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new FxRateExtractor().extractYearlyRate(document, CurrencyCode.USD));

        assertEquals("multiple FX rates found for USD", exception.getMessage());
    }

    private static TabularDocument monthlyRateDocument() {
        List<TabularTable> tables = new ArrayList<>();
        int index = 0;
        for (TaxYearPeriod period : TaxYearPeriod.values()) {
            tables.add(rateTable(period.name(), "USD", "1." + index));
            index++;
        }
        return new TabularDocument("monthly", tables);
    }

    private static TabularDocument yearlyRateDocument(TabularTable table) {
        return new TabularDocument("yearly", List.of(table));
    }

    private static TabularTable rateTable(String tableName, String currencyCode, String rate) {
        return new com.helper.util.api.CsvDocumentParser().parse(
                "Country/Territory,Currency,Currency Code,Currency Units per £1\n"
                        + "United States,Dollar," + currencyCode + "," + rate + "\n"
                        + "Hong Kong,Dollar,HKD,9.80\n",
                tableName);
    }
}
