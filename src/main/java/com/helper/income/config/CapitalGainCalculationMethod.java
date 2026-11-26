package com.helper.income.config;

import java.util.Objects;

/** Capital-gain calculation method metadata for one configured instrument. */
public class CapitalGainCalculationMethod {
    private final SecurityConfigEnums.CapitalGainCalculationMethodType type;

    /** Creates capital-gain calculation method metadata. */
    public CapitalGainCalculationMethod(SecurityConfigEnums.CapitalGainCalculationMethodType type) {
        this.type = Objects.requireNonNull(type, "type");
    }

    /** Returns the capital-gain calculation method type. */
    public SecurityConfigEnums.CapitalGainCalculationMethodType getType() {
        return type;
    }
}
