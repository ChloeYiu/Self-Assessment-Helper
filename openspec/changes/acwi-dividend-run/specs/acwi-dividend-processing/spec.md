# Spec Delta

## Purpose

Provide a reviewable UK Self Assessment dividend result for the configured ACWI accumulating fund imported from IBKR activity and an ERI report.

## ADDED Requirements

### Requirement: Supported ACWI dividend import
The system SHALL process configured ACWI ERI dividends only when the source, report, security, and required inputs are valid.

#### Scenario: Valid ACWI import
- **WHEN** valid ACWI source data, report, opening state, and FX inputs are supplied
- **THEN** the system returns a GBP dividend result and closing holding artifact

### Requirement: Reviewable ACWI import outcome
The system SHALL distinguish calculated contributions, ignored unrelated activity, review items, and assumptions.

#### Scenario: Missing opening holding
- **WHEN** the permitted opening-zero fallback is used
- **THEN** the import report identifies the security and boundary date as an assumption
