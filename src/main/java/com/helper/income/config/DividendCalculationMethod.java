package com.helper.income.config;

import java.util.Objects;

/** Dividend calculation method metadata for one configured instrument. */
public class DividendCalculationMethod {
    private final SecurityConfigEnums.DividendCalculationMethodType type;
    private final SecurityConfigEnums.EriReportFormat eriReportFormat;

    /** Creates dividend calculation method metadata. */
    public DividendCalculationMethod(SecurityConfigEnums.DividendCalculationMethodType type, SecurityConfigEnums.EriReportFormat eriReportFormat) {
        this.type = Objects.requireNonNull(type, "type");
        this.eriReportFormat = eriReportFormat;
        if (this.type == SecurityConfigEnums.DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND && this.eriReportFormat == null) {
            throw new IllegalArgumentException("eriReportFormat is required for non-UK accumulating fund dividends");
        }
    }

    /** Returns the dividend calculation method type. */
    public SecurityConfigEnums.DividendCalculationMethodType getType() {
        return type;
    }

    /** Returns the ERI report format, or null when this method does not use ERI reports. */
    public SecurityConfigEnums.EriReportFormat getEriReportFormat() {
        return eriReportFormat;
    }
}
