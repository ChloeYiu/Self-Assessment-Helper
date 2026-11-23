package com.helper.calculations;

import com.helper.pipeline.IncomeAssembler;
import com.helper.pipeline.investment.managed.ManagedInvestmentPipeline;
import com.helper.pipeline.investment.SelfDirectedInvestmentPipeline;
import com.helper.pipeline.TaxPipeline;
import com.helper.pipeline.TaxPipelineFactory;
import com.helper.pipeline.TaxPipelineResult;
import com.helper.pipeline.TaxPipelineType;
import com.helper.support.fx.FxRateProvider;
import com.helper.support.registry.Registry;
import com.helper.support.registry.RegistryKeys;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Top-level backend coordinator for one tax calculation run.
 *
 * It runs selected registered tax-pipeline domains with shared run resources.
 */
public class TaxCalculator {
    private static final Path DEFAULT_IBKR_CONFIG_PATH = Path.of("config/ibkr-flex.properties");
    private static final Map<TaxPipelineType, TaxPipelineFactory> PIPELINE_FACTORIES_BY_TYPE = Map.of(
            TaxPipelineType.SELF_DIRECTED_INVESTMENT,
            SelfDirectedInvestmentPipeline::new,
            TaxPipelineType.MANAGED_INVESTMENT,
            ManagedInvestmentPipeline::new);

    private final FxRateProvider fxRateProvider;
    private final Map<TaxPipelineType, TaxPipelineFactory> pipelineFactoriesByType;

    /** Creates the calculator with the application's registered category factories. */
    public TaxCalculator() {
        this(new FxRateProvider(), PIPELINE_FACTORIES_BY_TYPE);
    }

    TaxCalculator(
            FxRateProvider fxRateProvider,
            Map<TaxPipelineType, TaxPipelineFactory> pipelineFactoriesByType
    ) {
        this.fxRateProvider = Objects.requireNonNull(fxRateProvider, "fxRateProvider");
        this.pipelineFactoriesByType = Map.copyOf(Objects.requireNonNull(
                pipelineFactoriesByType, "pipelineFactoriesByType"));
    }

    /** Returns the top-level pipeline domains registered in this application instance. */
    public Set<TaxPipelineType> getAvailablePipelineTypes() {
        return pipelineFactoriesByType.keySet();
    }

    /** Runs every selected category with the local default IBKR Flex configuration. */
    public TaxCalculationResult run(TaxCalculationRequest request)
            throws IOException, InterruptedException {
        Registry registry = new Registry();
        registry.put(RegistryKeys.IBKR_CONFIG_PATH, DEFAULT_IBKR_CONFIG_PATH);
        return run(request, registry);
    }

    /** Runs every selected category using the caller-supplied run registry. */
    public TaxCalculationResult run(TaxCalculationRequest request, Registry registry)
            throws IOException, InterruptedException {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(registry, "registry");
        validateRequestedPipelineTypes(request);
        initializeRegistry(request, registry);
        return runAndAssembleSelectedPipelines(request, registry, pipelineFactoriesByType);
    }

    private void validateRequestedPipelineTypes(TaxCalculationRequest request) {
        for (TaxPipelineType pipelineType : request.selectedPipelineTypes()) {
            if (!pipelineFactoriesByType.containsKey(pipelineType)) {
                throw new IllegalArgumentException("pipeline type is not registered: " + pipelineType);
            }
        }
    }

    private void initializeRegistry(TaxCalculationRequest request, Registry registry)
            throws IOException, InterruptedException {
        if (!registry.contains(RegistryKeys.MONTHLY_FX_RATE_DOCUMENT)) {
            registry.put(
                    RegistryKeys.MONTHLY_FX_RATE_DOCUMENT,
                    fxRateProvider.fetchMonthlyRateDocument(request.taxYear()));
        }
        if (!registry.contains(RegistryKeys.YEARLY_FX_RATE_DOCUMENT)) {
            registry.put(
                    RegistryKeys.YEARLY_FX_RATE_DOCUMENT,
                    fxRateProvider.fetchYearlyRateDocument(request.taxYear()));
        }
    }

    private static TaxCalculationResult runAndAssembleSelectedPipelines(
            TaxCalculationRequest request,
            Registry registry,
            Map<TaxPipelineType, TaxPipelineFactory> pipelineFactoriesByType
    ) throws IOException, InterruptedException {
        IncomeAssembler assembler = new IncomeAssembler(request.taxYear());

        for (TaxPipelineType pipelineType : request.selectedPipelineTypes()) {
            TaxPipeline pipeline = Objects.requireNonNull(
                    pipelineFactoriesByType.get(pipelineType).create(registry),
                    "taxPipeline");
            TaxPipelineResult result = pipeline.run(request);
            assembler.add(result);
        }
        return assembler.build();
    }
}
