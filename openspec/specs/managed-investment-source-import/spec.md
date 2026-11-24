# managed-investment-source-import Specification

## Purpose
Import provider-produced managed-investment sources while preserving their reported income, capital-gains, and provenance evidence.

## Requirements

### Requirement: Managed-investment source import
The system SHALL let `ManagedInvestmentPipeline` load selected provider sources through the matching provider-specific importer and return their reported tax facts with source evidence.

#### Scenario: JPMPI annual tax pack
- **WHEN** a caller supplies a JPMPI annual tax pack for the selected tax year
- **THEN** `ManagedInvestmentPipeline` invokes the JPMPI importer and returns the pack's reported income, capital-gains, provenance, and any recoverable issues
