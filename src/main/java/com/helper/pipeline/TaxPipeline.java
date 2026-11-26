package com.helper.pipeline;

import com.helper.calculations.TaxCalculationRequest;
import java.io.IOException;

/** Prepares source-level results for one registered top-level tax pipeline. */
public interface TaxPipeline {
    TaxPipelineResult run(TaxCalculationRequest request)
            throws IOException, InterruptedException;
}
