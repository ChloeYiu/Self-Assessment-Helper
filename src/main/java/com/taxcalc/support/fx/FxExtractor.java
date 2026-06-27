package com.taxcalc.support.fx;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.YearMonth;

/**
 * FX extractor draft.
 *
 * Keep as a reference draft for the rate URL and fetch flow.
 */
public class FxExtractor {

    private static final String BASE_URL = "https://www.trade-tariff.service.gov.uk/uk/api/exchange_rates/files/";

    private final HttpClient httpClient;

    public FxExtractor() {
        this(HttpClient.newHttpClient());
    }

    public FxExtractor(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public BigDecimal getMonthlyRate(String currencyCode, YearMonth month) throws IOException, InterruptedException {
        String csv = fetchCsv("monthly_csv_" + formatMonth(month) + ".csv");
        return extractMonthlyRate(csv, currencyCode, month);
    }

    public BigDecimal getYearlyAverageRate(String currencyCode, YearMonth month)
            throws IOException, InterruptedException {
        String csv = fetchCsv("average_csv_" + formatMonth(month) + ".csv");
        return extractYearlyAverageRate(csv, currencyCode, month);
    }

    private BigDecimal extractMonthlyRate(String csv, String currencyCode, YearMonth month) {
        throw new UnsupportedOperationException();
    }

    private BigDecimal extractYearlyAverageRate(String csv, String currencyCode, YearMonth month) {
        throw new UnsupportedOperationException();
    }

    private String fetchCsv(String fileName) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + fileName))
                .header("User-Agent", "Tax-Calculator/1.0")
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("HMRC FX request failed: HTTP " + response.statusCode());
        }
        return response.body();
    }

    private String formatMonth(YearMonth month) {
        return month.getYear() + "-" + month.getMonthValue();
    }
}