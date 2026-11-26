package com.helper.calculations;

import com.helper.config.TaxYear;
import com.helper.pipeline.TaxPipelineType;
import java.util.Objects;
import java.util.Set;

/** User-selected scope for one tax calculation run. */
public record TaxCalculationRequest(TaxYear taxYear, Set<TaxPipelineType> selectedPipelineTypes) {
    public TaxCalculationRequest {
        taxYear = Objects.requireNonNull(taxYear, "taxYear");
        selectedPipelineTypes = Set.copyOf(Objects.requireNonNull(
                selectedPipelineTypes, "selectedPipelineTypes"));
        if (selectedPipelineTypes.isEmpty()) {
            throw new IllegalArgumentException("selectedPipelineTypes must not be empty");
        }
    }
}
