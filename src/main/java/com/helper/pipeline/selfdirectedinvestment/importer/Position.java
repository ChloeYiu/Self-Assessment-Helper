package com.helper.pipeline.selfdirectedinvestment.importer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A provider-reported holding quantity for one account, security, and cutoff date.
 */
public record Position(
        String source,
        String accountId,
        String isin,
        String symbol,
        BigDecimal quantity,
        LocalDate positionDate
) {
    public Position {
        source = requireText(source, "source");
        accountId = requireText(accountId, "accountId");
        isin = requireText(isin, "isin");
        symbol = requireText(symbol, "symbol");
        quantity = Objects.requireNonNull(quantity, "quantity").stripTrailingZeros();
        positionDate = Objects.requireNonNull(positionDate, "positionDate");
    }

    private static String requireText(String value, String fieldName) {
        String text = Objects.requireNonNull(value, fieldName).trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return text;
    }
}
