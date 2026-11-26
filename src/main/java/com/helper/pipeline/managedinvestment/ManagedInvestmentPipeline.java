package com.helper.pipeline.managedinvestment;

import com.helper.calculations.TaxCalculationRequest;
import com.helper.pipeline.managedinvestment.jpmpi.JpmpiImporter;
import com.helper.pipeline.TaxPipeline;
import com.helper.support.registry.Registry;
import com.helper.support.registry.RegistryKeys;
import com.helper.util.pdf.Pdf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Prepares source results from selected managed-investment providers.
 *
 * <p>Source selection is supplied through the run registry.</p>
 */
public final class ManagedInvestmentPipeline implements TaxPipeline {
    private final ManagedInvestmentPipelineInput input;
    private final JpmpiImporter jpmpiImporter;
    private final ManagedInvestmentResultAssembler resultAssembler;

    /** Creates the registry-bound managed-investment pipeline. */
    public ManagedInvestmentPipeline(Registry registry) {
        this(
                Objects.requireNonNull(registry, "registry")
                        .require(RegistryKeys.MANAGED_INVESTMENT_PIPELINE_INPUT),
                new JpmpiImporter(),
                new ManagedInvestmentResultAssembler());
    }

    ManagedInvestmentPipeline(
            ManagedInvestmentPipelineInput input,
            JpmpiImporter jpmpiImporter,
            ManagedInvestmentResultAssembler resultAssembler
    ) {
        this.input = Objects.requireNonNull(input, "input");
        this.jpmpiImporter = Objects.requireNonNull(jpmpiImporter, "jpmpiImporter");
        this.resultAssembler = Objects.requireNonNull(resultAssembler, "resultAssembler");
    }

    @Override
    public ManagedInvestmentPipelineResult run(TaxCalculationRequest request)
            throws IOException, InterruptedException {
        TaxCalculationRequest calculationRequest = Objects.requireNonNull(request, "request");
        List<ManagedInvestmentSourceResult> sourceResults = new ArrayList<>();
        for (Pdf pdf : input.getPdfs()) {
            sourceResults.add(jpmpiImporter.importSource(pdf));
        }
        return resultAssembler.assemble(calculationRequest.taxYear(), sourceResults);
    }
}
