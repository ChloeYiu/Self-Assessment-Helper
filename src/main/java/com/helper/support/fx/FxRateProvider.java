package com.helper.support.fx;

import com.helper.config.TaxYear;
import com.helper.config.TaxYearPeriod;
import com.helper.util.api.ApiRequest;
import com.helper.util.api.CsvDocumentParser;
import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularTable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fetches FX rate CSV files and parses them into tabular documents.
 */
public class FxRateProvider {
    static final String YEARLY_AVERAGE_TABLE_NAME = "YEARLY_AVERAGE";

    private static final String BASE_URL = "https://www.trade-tariff.service.gov.uk/uk/api/exchange_rates/files/";

    private final HttpClient httpClient;
    private final CsvDocumentParser csvDocumentParser;

    public FxRateProvider() {
        this(HttpClient.newHttpClient());
    }

    public FxRateProvider(HttpClient httpClient) {
        this(httpClient, new CsvDocumentParser());
    }

    public FxRateProvider(HttpClient httpClient, CsvDocumentParser csvDocumentParser) {
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

    private String fetchMonthlyCsv(YearMonth month) throws IOException, InterruptedException {
        URI uri = URI.create(BASE_URL + "monthly_csv_" + month.getYear() + "-" + month.getMonthValue() + ".csv");
        return ApiRequest.sendGet(httpClient, uri, "HMRC monthly FX request failed");
    }

    private String fetchYearlyCsv(TaxYear taxYear) throws IOException, InterruptedException {
        URI uri = URI.create(BASE_URL + "average_csv_" + (taxYear.getStartYear() + 1) + "-3.csv");
        return ApiRequest.sendGet(httpClient, uri, "HMRC yearly average FX request failed");
    }
}
