package com.helper.pipeline.investment.managed;

import com.helper.calculations.TaxCalculationRequest;
import com.helper.pipeline.TaxPipeline;
import com.helper.pipeline.TaxPipelineResult;
import com.helper.support.registry.Registry;
import java.io.IOException;

/**
 * Prepares tax-pack results from selected managed-investment providers.
 *
 * <p>Tax-pack source selection and provider import are not implemented yet.</p>
 */
public final class ManagedInvestmentPipeline implements TaxPipeline {
    /** Creates the registry-bound managed-investment pipeline. */
    public ManagedInvestmentPipeline(Registry registry) {
    }

    @Override
    public TaxPipelineResult run(TaxCalculationRequest request)
            throws IOException, InterruptedException {
        throw new UnsupportedOperationException("managed-investment import is not implemented yet");
    }
}
