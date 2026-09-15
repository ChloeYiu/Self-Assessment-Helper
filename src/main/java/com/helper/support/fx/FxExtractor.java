package com.helper.support.fx;

import com.helper.config.CurrencyCode;
import com.helper.config.TaxYear;
import com.helper.config.TaxYearPeriod;
import com.helper.util.api.ApiRequest;
import com.helper.util.api.CsvDocumentParser;
import com.helper.util.table.TabularCell;
import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularTable;
import com.helper.util.table.TabularRow;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Extracts HMRC exchange-rate CSV files into reusable tax-year tabular documents and rate lookups.
 */
public class FxExtractor {

    private static final String BASE_URL = "https://www.trade-tariff.service.gov.uk/uk/api/exchange_rates/files/";
    private static final String YEARLY_AVERAGE_TABLE_NAME = "YEARLY_AVERAGE";
    private static final String RATE_COLUMN_NAME = "Currency Units per £1";
    private static final String CURRENCY_CODE_COLUMN_NAME = "Currency Code";

    private final HttpClient httpClient;
    private final CsvDocumentParser csvDocumentParser;

    public FxExtractor() {
        this(HttpClient.newHttpClient());
    }

    public FxExtractor(HttpClient httpClient) {
        this(httpClient, new CsvDocumentParser());
    }

    public FxExtractor(HttpClient httpClient, CsvDocumentParser csvDocumentParser) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.csvDocumentParser = Objects.requireNonNull(csvDocumentParser, "csvDocumentParser");
    }

    public TabularDocument fetchMonthlyRateDocument(TaxYear taxYear) throws IOException, InterruptedException {
        TaxYear year = Objects.requireNonNull(taxYear, "taxYear");
        List<TabularTable> tables = new ArrayList<>();

        for (TaxYearPeriod period : TaxYearPeriod.values()) {
            String csv = fetchMonthlyCsv(period.toYearMonth(year));
            tables.add(csvDocumentParser.parse(csv, period.name()));
        }

        return new TabularDocument("HMRC monthly FX " + year.getStartYear(), tables);
    }

    public TabularDocument fetchYearlyRateDocument(TaxYear taxYear) throws IOException, InterruptedException {
        TaxYear year = Objects.requireNonNull(taxYear, "taxYear");
        String csv = fetchYearlyCsv(year);
        TabularTable table = csvDocumentParser.parse(csv, YEARLY_AVERAGE_TABLE_NAME);

        return new TabularDocument("HMRC yearly average FX " + year.getStartYear(), List.of(table));
    }

    public Map<TaxYearPeriod, BigDecimal> extractMonthlyRates(
            TabularDocument document,
            CurrencyCode currencyCode
    ) {
        TabularDocument rateDocument = Objects.requireNonNull(document, "document");
        CurrencyCode code = Objects.requireNonNull(currencyCode, "currencyCode");
        Map<TaxYearPeriod, BigDecimal> rates = new EnumMap<>(TaxYearPeriod.class);

        for (TaxYearPeriod period : TaxYearPeriod.values()) {
            TabularTable table = rateDocument.getTable(period.name())
                    .orElseThrow(() -> new IllegalArgumentException("FX document must contain table " + period.name()));
            rates.put(period, extractRate(table, code));
        }

        return rates;
    }

    public BigDecimal extractYearlyRate(TabularDocument document, CurrencyCode currencyCode) {
        TabularDocument rateDocument = Objects.requireNonNull(document, "document");
        CurrencyCode code = Objects.requireNonNull(currencyCode, "currencyCode");
        TabularTable table = rateDocument.getTable(YEARLY_AVERAGE_TABLE_NAME)
                .orElseThrow(() -> new IllegalArgumentException(
                        "FX document must contain table " + YEARLY_AVERAGE_TABLE_NAME));

        return extractRate(table, code);
    }

    private BigDecimal extractRate(TabularTable table, CurrencyCode currencyCode) {
        List<TabularRow> rows = table.getRows()
                .stream()
                .filter(candidate -> hasCurrencyCode(candidate, currencyCode))
                .toList();

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("FX rate not found for " + currencyCode);
        }

        if (rows.size() > 1) {
            throw new IllegalArgumentException("multiple FX rates found for " + currencyCode);
        }

        return readRate(rows.getFirst(), currencyCode);
    }

    private boolean hasCurrencyCode(TabularRow row, CurrencyCode currencyCode) {
        return row.getCell(CURRENCY_CODE_COLUMN_NAME)
                .flatMap(TabularCell::getNormalizedText)
                .filter(currencyCode.name()::equals)
                .isPresent();
    }

    private BigDecimal readRate(TabularRow row, CurrencyCode currencyCode) {
        return row.getCell(RATE_COLUMN_NAME)
                .flatMap(TabularCell::getBigDecimal)
                .orElseThrow(() -> new IllegalArgumentException("missing HMRC FX rate for " + currencyCode));
    }

    private String fetchMonthlyCsv(YearMonth month) throws IOException, InterruptedException {
        URI uri = URI.create(BASE_URL + "monthly_csv_" + month.getYear() + "-" + month.getMonthValue() + ".csv");
        return ApiRequest.sendGet(httpClient, uri, "HMRC monthly FX request failed");
    }

    private String fetchYearlyCsv(TaxYear taxYear) throws IOException, InterruptedException {
        URI uri = URI.create(BASE_URL + "average_csv_" + (taxYear.getStartYear() + 1) + "-3.csv");
        return ApiRequest.sendGet(httpClient, uri, "HMRC yearly average FX request failed");
    }
}
