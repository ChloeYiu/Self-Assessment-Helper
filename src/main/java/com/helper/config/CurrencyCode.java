package com.helper.config;

import java.util.Objects;

/**
 * Currency codes supported by the Self Assessment helper.
 */
public enum CurrencyCode {
    GBP,
    HKD,
    USD;

    /** Parses a supported currency code. */
    public static CurrencyCode parse(String value, String errorContext) {
        String code = Objects.requireNonNull(value, "value").trim();
        try {
            return CurrencyCode.valueOf(code);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "unsupported " + Objects.requireNonNull(errorContext, "errorContext") + " currency: " + value,
                    exception);
        }
    }
}
