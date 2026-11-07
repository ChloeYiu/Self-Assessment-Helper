package com.helper.pipeline.investment.dividend;

import com.helper.income.config.SecurityConfigEnums.DividendCalculationMethodType;
import com.helper.pipeline.investment.dividend.method.NonUkAccumulatingFundDividendPipeline;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Routes a configured dividend method to its registered preparation strategy. */
public class DividendMethodDispatcher {
    private final Map<DividendCalculationMethodType, DividendMethodPipeline> pipelinesByMethod;

    /** Registers the dividend-method pipelines currently supported by the application. */
    public DividendMethodDispatcher() {
        this(Map.of(
                DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND,
                new NonUkAccumulatingFundDividendPipeline()));
    }

    /** Creates a dispatcher with explicit routes for package-level tests. */
    DividendMethodDispatcher(Map<DividendCalculationMethodType, DividendMethodPipeline> pipelinesByMethod) {
        this.pipelinesByMethod = Map.copyOf(Objects.requireNonNull(pipelinesByMethod, "pipelinesByMethod"));
    }

    /**
     * Finds the strategy registered for one configured dividend method.
     *
     * @param methodType configured dividend method
     * @return the strategy when this application can prepare that method
     */
    public Optional<DividendMethodPipeline> findPipeline(DividendCalculationMethodType methodType) {
        return Optional.ofNullable(pipelinesByMethod.get(
                Objects.requireNonNull(methodType, "methodType")));
    }
}
