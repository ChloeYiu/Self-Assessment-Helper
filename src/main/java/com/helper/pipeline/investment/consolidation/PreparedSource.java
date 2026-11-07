package com.helper.pipeline.investment.consolidation;

import com.helper.pipeline.investment.importer.SourceImportResult;
import com.helper.pipeline.investment.support.SecuritySupportResult;

/**
 * Pairs one provider import with the support decisions made for that import.
 */
public record PreparedSource(
        SourceImportResult sourceImportResult,
        SecuritySupportResult securitySupportResult
) {
}
