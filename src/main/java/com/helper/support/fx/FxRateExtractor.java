package com.helper.support.fx;

import com.helper.config.CurrencyCode;
import com.helper.config.TaxYearPeriod;
import com.helper.util.table.TabularCell;
import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularRow;
import com.helper.util.table.TabularTable;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Extracts FX rates from already parsed tabular rate documents.
 */
public class FxRateExtractor {
    private static final String RATE_COLUMN_NAME = "Currency Units per £1";
    private static final String CURRENCY_CODE_COLUMN_NAME = "Currency Code";

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
        TabularTable table = rateDocument.getTable(FxRateProvider.YEARLY_AVERAGE_TABLE_NAME)
                .orElseThrow(() -> new IllegalArgumentException(
                        "FX document must contain table " + FxRateProvider.YEARLY_AVERAGE_TABLE_NAME));

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
}
