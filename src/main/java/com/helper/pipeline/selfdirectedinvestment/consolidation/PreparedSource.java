package com.helper.pipeline.selfdirectedinvestment.consolidation;

import com.helper.pipeline.selfdirectedinvestment.importer.SourceImportResult;
import com.helper.pipeline.selfdirectedinvestment.support.SecuritySupportResult;

/**
 * Pairs one provider import with the support decisions made for that import.
 */
public record PreparedSource(
        SourceImportResult sourceImportResult,
        SecuritySupportResult securitySupportResult
) {
}
