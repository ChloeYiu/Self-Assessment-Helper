package com.helper.support.registry;

import com.helper.pipeline.managedinvestment.ManagedInvestmentPipelineInput;
import com.helper.util.table.TabularDocument;
import java.nio.file.Path;

/**
 * Shared registry keys used across tax calculation runs.
 */
public final class RegistryKeys {
    public static final RegistryKey<Path> IBKR_CONFIG_PATH =
            RegistryKey.of("ibkrConfigPath", Path.class);

    public static final RegistryKey<ManagedInvestmentPipelineInput> MANAGED_INVESTMENT_PIPELINE_INPUT =
            RegistryKey.of("managedInvestmentPipelineInput", ManagedInvestmentPipelineInput.class);

    public static final RegistryKey<TabularDocument> MONTHLY_FX_RATE_DOCUMENT =
            RegistryKey.of("monthlyFxRateDocument", TabularDocument.class);

    public static final RegistryKey<TabularDocument> YEARLY_FX_RATE_DOCUMENT =
            RegistryKey.of("yearlyFxRateDocument", TabularDocument.class);

    private RegistryKeys() {
    }
}
