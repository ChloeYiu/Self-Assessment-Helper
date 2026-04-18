package com.taxcalc.fx.implementation;

import com.taxcalc.fx.FxRateProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.YearMonth;

/**
 * HMRC Trade Tariff CSV-backed FX provider.
 */
public class HmrcTradeTariffFxRateProvider implements FxRateProvider {

    private static final String BASE_URL = "https://www.trade-tariff.service.gov.uk/uk/api/exchange_rates/files/";
    private static final int CURRENCY_COLUMN_INDEX = 2;
    private static final int MONTHLY_RATE_COLUMN_INDEX = 3;
    private static final int YEARLY_RATE_COLUMN_INDEX = 4;

    private final HttpClient httpClient;

    public HmrcTradeTariffFxRateProvider() {
        this(HttpClient.newHttpClient());
    }

    public HmrcTradeTariffFxRateProvider(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public BigDecimal getMonthlyRate(String currencyCode, YearMonth month) throws IOException, InterruptedException {
        String csv = fetchCsv("monthly_csv_" + formatMonth(month) + ".csv");
        return extractRate(csv, currencyCode, MONTHLY_RATE_COLUMN_INDEX);
    }

    @Override
    public BigDecimal getYearlyAverageRate(String currencyCode, YearMonth month) throws IOException, InterruptedException {
        String csv = fetchCsv("average_csv_" + formatMonth(month) + ".csv");
        return extractRate(csv, currencyCode, YEARLY_RATE_COLUMN_INDEX);
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

    private BigDecimal extractRate(String csv, String currencyCode, int rateColumnIndex) {
        if (csv == null || csv.isBlank()) {
            throw new IllegalArgumentException("FX CSV content is empty");
        }

        String normalizedCurrency = currencyCode == null ? "" : currencyCode.trim().toUpperCase();
        String[] lines = csv.split("\\R");

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] cells = splitCsvLine(line);
            if (cells.length <= rateColumnIndex || cells.length <= CURRENCY_COLUMN_INDEX) {
                continue;
            }

            String rowCurrency = unquote(cells[CURRENCY_COLUMN_INDEX]).trim().toUpperCase();
            if (!normalizedCurrency.equals(rowCurrency)) {
                continue;
            }

            String rateText = unquote(cells[rateColumnIndex]).trim();
            if (rateText.isEmpty()) {
                break;
            }

            return new BigDecimal(rateText);
        }

        throw new IllegalArgumentException("Rate not found for currency: " + currencyCode);
    }

    private String formatMonth(YearMonth month) {
        return month.getYear() + "-" + month.getMonthValue();
    }

    private String unquote(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private String[] splitCsvLine(String line) {
        java.util.List<String> cells = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
                continue;
            }

            if (c == ',' && !inQuotes) {
                cells.add(current.toString());
                current.setLength(0);
                continue;
            }

            current.append(c);
        }

        cells.add(current.toString());
        return cells.toArray(new String[0]);
    }
}
