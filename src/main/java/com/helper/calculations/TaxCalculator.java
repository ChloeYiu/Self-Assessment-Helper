package com.helper.calculations;

import com.helper.income.IncomeType;
import com.helper.pipeline.IncomeAssembler;
import com.helper.pipeline.investment.InvestmentPipeline;
import com.helper.pipeline.TaxPipeline;
import com.helper.pipeline.TaxPipelineFactory;
import com.helper.pipeline.TaxPipelineResult;
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
 * It runs selected registered income categories with shared run resources.
 */
public class TaxCalculator {
    private static final Path DEFAULT_IBKR_CONFIG_PATH = Path.of("config/ibkr-flex.properties");
    private static final Map<IncomeType, TaxPipelineFactory> PIPELINE_FACTORIES_BY_INCOME_TYPE = Map.of(
            IncomeType.DIVIDENDS,
            InvestmentPipeline::new);

    private final FxRateProvider fxRateProvider;
    private final Map<IncomeType, TaxPipelineFactory> pipelineFactoriesByIncomeType;

    /** Creates the calculator with the application's registered category factories. */
    public TaxCalculator() {
        this(new FxRateProvider());
    }

    TaxCalculator(FxRateProvider fxRateProvider) {
        this(fxRateProvider, PIPELINE_FACTORIES_BY_INCOME_TYPE);
    }

    TaxCalculator(
            FxRateProvider fxRateProvider,
            Map<IncomeType, TaxPipelineFactory> pipelineFactoriesByIncomeType
    ) {
        this.fxRateProvider = Objects.requireNonNull(fxRateProvider, "fxRateProvider");
        this.pipelineFactoriesByIncomeType = Map.copyOf(Objects.requireNonNull(
                pipelineFactoriesByIncomeType, "pipelineFactoriesByIncomeType"));
    }

    /** Returns the income categories registered in this application instance. */
    public Set<IncomeType> getAvailableIncomeTypes() {
        return pipelineFactoriesByIncomeType.keySet();
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
        validateRequestedIncomeTypes(request);
        initializeRegistry(request, registry);
        return runAndAssembleSelectedPipelines(request, registry, pipelineFactoriesByIncomeType);
    }

    private void validateRequestedIncomeTypes(TaxCalculationRequest request) {
        for (IncomeType incomeType : request.selectedIncomeTypes()) {
            if (!pipelineFactoriesByIncomeType.containsKey(incomeType)) {
                throw new IllegalArgumentException("income type is not registered: " + incomeType);
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
            Map<IncomeType, TaxPipelineFactory> pipelineFactoriesByIncomeType
    ) throws IOException, InterruptedException {
        IncomeAssembler assembler = new IncomeAssembler(request.taxYear());

        for (IncomeType incomeType : request.selectedIncomeTypes()) {
            TaxPipeline pipeline = Objects.requireNonNull(
                    pipelineFactoriesByIncomeType.get(incomeType).create(registry),
                    "taxPipeline");
            TaxPipelineResult result = pipeline.run(request);
            assembler.add(result);
        }
        return assembler.build();
    }
}
