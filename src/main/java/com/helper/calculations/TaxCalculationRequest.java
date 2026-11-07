package com.helper.calculations;

import com.helper.config.TaxYear;
import com.helper.income.IncomeType;
import java.util.Objects;
import java.util.Set;

/** User-selected scope for one tax calculation run. */
public record TaxCalculationRequest(TaxYear taxYear, Set<IncomeType> selectedIncomeTypes) {
    public TaxCalculationRequest {
        taxYear = Objects.requireNonNull(taxYear, "taxYear");
        selectedIncomeTypes = Set.copyOf(Objects.requireNonNull(
                selectedIncomeTypes, "selectedIncomeTypes"));
        if (selectedIncomeTypes.isEmpty()) {
            throw new IllegalArgumentException("selectedIncomeTypes must not be empty");
        }
    }
}
