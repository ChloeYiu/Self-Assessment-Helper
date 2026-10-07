package com.helper.pipeline;

import com.helper.config.TaxYear;
import com.helper.income.implementation.capitalgain.model.CarryForwardSnapshot;
import com.helper.income.implementation.dividend.model.SecurityHoldingSnapshot;
import com.helper.util.ListUtils;
import java.util.List;
import java.util.Objects;

/**
 * Shared tax-run input, separate from provider credentials and retrieval settings.
 */
public class TaxPipelineInput {
    private final TaxYear taxYear;
    private final List<SecurityHoldingSnapshot> openingHoldingSnapshots;
    private final List<CarryForwardSnapshot> openingCapitalGainSnapshots;

    /** Creates the shared tax-year input and optional opening state. */
    public TaxPipelineInput(
            TaxYear taxYear,
            List<SecurityHoldingSnapshot> openingHoldingSnapshots,
            List<CarryForwardSnapshot> openingCapitalGainSnapshots
    ) {
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
        this.openingHoldingSnapshots = ListUtils.copyOrEmpty(openingHoldingSnapshots);
        this.openingCapitalGainSnapshots = ListUtils.copyOrEmpty(openingCapitalGainSnapshots);
    }

    /** Returns the tax year for the calculation run. */
    public TaxYear getTaxYear() {
        return taxYear;
    }

    /** Returns supplied opening dividend-holding snapshots. */
    public List<SecurityHoldingSnapshot> getOpeningHoldingSnapshots() {
        return List.copyOf(openingHoldingSnapshots);
    }

    /** Returns supplied opening capital-gain carry-forward snapshots. */
    public List<CarryForwardSnapshot> getOpeningCapitalGainSnapshots() {
        return List.copyOf(openingCapitalGainSnapshots);
    }
}
