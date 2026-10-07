package com.helper.calculations;

import com.helper.config.TaxYear;
import com.helper.support.fx.FxRateProvider;
import com.helper.support.registry.Registry;
import com.helper.support.registry.RegistryKeys;

import java.io.IOException;
import java.util.Objects;

/**
 * Top-level backend coordinator for one tax calculation run.
 *
 * It owns the run registry and will later initialize shared upstream data before executing pipelines.
 */
public class TaxCalculator {
    private final TaxYear taxYear;
    private final Registry registry;

    /** Creates a tax calculator for one tax year with a fresh run registry. */
    public TaxCalculator(TaxYear taxYear) {
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
        this.registry = new Registry();
    }

    /** Runs the full tax calculation flow for this tax year. */
    public TaxCalculationResult run() throws IOException, InterruptedException {
        initializeRegistry();
        return new TaxCalculationResult();
    }

    /** Initializes shared registry values needed by pipelines and calculators. */
    private void initializeRegistry() throws IOException, InterruptedException {
        FxRateProvider fxRateProvider = new FxRateProvider();
        registry.put(
                RegistryKeys.MONTHLY_FX_RATE_DOCUMENT,
                fxRateProvider.fetchMonthlyRateDocument(taxYear));
        registry.put(
                RegistryKeys.YEARLY_FX_RATE_DOCUMENT,
                fxRateProvider.fetchYearlyRateDocument(taxYear));
    }

    /** Returns the tax year being processed. */
    public TaxYear getTaxYear() {
        return taxYear;
    }

    /** Returns this run's generic registry. */
    public Registry getRegistry() {
        return registry;
    }
}
