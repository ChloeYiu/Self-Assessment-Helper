package com.helper.pipeline.investment;

import com.helper.calculations.TaxCalculationRequest;
import com.helper.issue.Issue;
import com.helper.pipeline.TaxPipeline;
import com.helper.pipeline.TaxPipelineResult;
import com.helper.pipeline.investment.assembly.InvestmentAssemblyPipeline;
import com.helper.pipeline.investment.consolidation.SourceConsolidationResult;
import com.helper.pipeline.investment.dividend.InvestmentDividendPipeline;
import com.helper.pipeline.investment.dividend.InvestmentDividendResult;
import com.helper.support.registry.Registry;
import com.helper.support.registry.RegistryKeys;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Prepares all investment sources from one shared investment assembly pass.
 *
 * <p>The current implementation prepares dividends. Future capital-gain preparation reuses the
 * same consolidated source result before this pipeline returns its result.</p>
 */
public final class InvestmentPipeline implements TaxPipeline {
    private final InvestmentAssemblyPipeline assemblyPipeline;
    private final InvestmentDividendPipeline dividendPipeline;

    /** Creates the registry-bound investment pipeline for the selected IBKR configuration. */
    public InvestmentPipeline(Registry registry) throws IOException {
        this.assemblyPipeline = new InvestmentAssemblyPipeline(
                registry.require(RegistryKeys.IBKR_CONFIG_PATH));
        this.dividendPipeline = new InvestmentDividendPipeline(registry);
    }

    @Override
    public TaxPipelineResult run(TaxCalculationRequest request)
            throws IOException, InterruptedException {
        SourceConsolidationResult sourceResult = assemblyPipeline.assemble(
                new TaxPipelineInput(request.taxYear(), List.of()));
        InvestmentDividendResult dividendResult = dividendPipeline.prepare(sourceResult);
        List<Issue> issues = new ArrayList<>(sourceResult.issues());
        issues.addAll(dividendResult.issues());
        return new InvestmentPipelineResult(dividendResult.dividendSources(), issues);
    }
}
