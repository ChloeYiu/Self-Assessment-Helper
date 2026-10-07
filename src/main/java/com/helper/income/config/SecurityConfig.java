package com.helper.income.config;

import java.util.Objects;

/** Tax calculation metadata for one configured instrument. */
public class SecurityConfig {
    private final String isin;
    private final DividendCalculationMethod dividendCalculationMethod;
    private final CapitalGainCalculationMethod capitalGainCalculationMethod;

    /** Creates tax calculation metadata for one instrument. */
    public SecurityConfig(
            String isin,
            DividendCalculationMethod dividendCalculationMethod,
            CapitalGainCalculationMethod capitalGainCalculationMethod
    ) {
        this.isin = requireNonBlank(isin, "isin");
        this.dividendCalculationMethod = dividendCalculationMethod == null
                ? new DividendCalculationMethod(SecurityConfigEnums.DividendCalculationMethodType.UNSUPPORTED, null)
                : dividendCalculationMethod;
        this.capitalGainCalculationMethod = capitalGainCalculationMethod == null
                ? new CapitalGainCalculationMethod(SecurityConfigEnums.CapitalGainCalculationMethodType.UNSUPPORTED)
                : capitalGainCalculationMethod;
    }

    /** Returns the configured instrument ISIN. */
    public String getIsin() {
        return isin;
    }

    /** Returns the dividend calculation method. */
    public DividendCalculationMethod getDividendCalculationMethod() {
        return dividendCalculationMethod;
    }

    /** Returns the capital-gain calculation method. */
    public CapitalGainCalculationMethod getCapitalGainCalculationMethod() {
        return capitalGainCalculationMethod;
    }

    private static String requireNonBlank(String value, String name) {
        String result = Objects.requireNonNull(value, name).trim();
        if (result.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return result;
    }
}
