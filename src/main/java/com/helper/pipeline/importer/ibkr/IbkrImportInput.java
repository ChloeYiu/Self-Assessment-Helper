package com.helper.pipeline.importer.ibkr;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Objects;

/**
 * IBKR-specific configuration and the date range to retrieve from Flex.
 */
public class IbkrImportInput {
    private final Path configPath;
    private final LocalDate requestedStartDate;
    private final LocalDate requestedEndDate;

    /**
     * Creates an IBKR import request.
     *
     * @param configPath optional IBKR Flex configuration path; null selects the default configuration
     * @param requestedStartDate first date requested from IBKR
     * @param requestedEndDate last date requested from IBKR
     */
    public IbkrImportInput(Path configPath, LocalDate requestedStartDate, LocalDate requestedEndDate) {
        this.configPath = configPath;
        this.requestedStartDate = Objects.requireNonNull(requestedStartDate, "requestedStartDate");
        this.requestedEndDate = Objects.requireNonNull(requestedEndDate, "requestedEndDate");
        if (requestedEndDate.isBefore(requestedStartDate)) {
            throw new IllegalArgumentException("requestedEndDate must not be before requestedStartDate");
        }
    }

    /** Returns the optional IBKR Flex configuration path. */
    public Path getConfigPath() {
        return configPath;
    }

    /** Returns the first date requested from IBKR. */
    public LocalDate getRequestedStartDate() {
        return requestedStartDate;
    }

    /** Returns the last date requested from IBKR. */
    public LocalDate getRequestedEndDate() {
        return requestedEndDate;
    }
}
