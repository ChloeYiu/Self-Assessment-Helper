package com.helper.pipeline.consolidation;

import com.helper.pipeline.importer.SourceImportResult;
import com.helper.pipeline.support.SecuritySupportResult;

/**
 * Pairs one provider import with the support decisions made for that import.
 */
public record PreparedSource(
        SourceImportResult sourceImportResult,
        SecuritySupportResult securitySupportResult
) {
}
