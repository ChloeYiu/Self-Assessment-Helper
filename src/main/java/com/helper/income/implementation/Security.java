package com.helper.income.implementation;

import com.helper.config.CurrencyCode;
import java.util.Objects;

public class Security {
    private final String identifier;
    private final String ticker;
    private final String name;
    private final CurrencyCode currencyCode;

    public Security(String identifier, String ticker, String name, CurrencyCode currencyCode) {
        this.identifier = Objects.requireNonNull(identifier, "identifier");
        this.ticker = Objects.requireNonNull(ticker, "ticker");
        this.name = Objects.requireNonNull(name, "name");
        this.currencyCode = Objects.requireNonNull(currencyCode, "currencyCode");
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getTicker() {
        return ticker;
    }

    public String getName() {
        return name;
    }

    public CurrencyCode getCurrencyCode() {
        return currencyCode;
    }
}
