package com.helper.support.registry;

import com.helper.util.table.TabularDocument;

/**
 * Shared registry keys used across tax calculation runs.
 */
public final class RegistryKeys {
    public static final RegistryKey<TabularDocument> MONTHLY_FX_RATE_DOCUMENT =
            RegistryKey.of("monthlyFxRateDocument", TabularDocument.class);

    public static final RegistryKey<TabularDocument> YEARLY_FX_RATE_DOCUMENT =
            RegistryKey.of("yearlyFxRateDocument", TabularDocument.class);

    private RegistryKeys() {
    }
}
