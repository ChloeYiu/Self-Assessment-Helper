package com.helper.income.config;

/** Enum values used by security configuration. */
public final class SecurityConfigEnums {
    private SecurityConfigEnums() {
    }

    /** Supported dividend calculation method types for configured instruments. */
    public enum DividendCalculationMethodType {
        NON_UK_ACCUMULATING_FUND,
        UNSUPPORTED
    }

    /** Supported capital-gain calculation method types for configured instruments. */
    public enum CapitalGainCalculationMethodType {
        STANDARD_SECURITY_POOLING,
        UNSUPPORTED
    }

    /** Supported ERI report formats for configured instruments. */
    public enum EriReportFormat {
        SSGA_SPDR_UK_REPORTABLE_INCOME
    }
}
