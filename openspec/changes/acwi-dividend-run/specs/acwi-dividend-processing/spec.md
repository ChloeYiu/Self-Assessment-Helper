# Spec Delta

## Purpose

Provide a reviewable UK Self Assessment dividend result for the configured ACWI accumulating fund imported from IBKR activity and an ERI report.

## ADDED Requirements

### Requirement: Supported ACWI dividend import
The system SHALL process configured ACWI ERI dividends only when the source, report, security, and required inputs are valid.

#### Scenario: Valid ACWI import
- **WHEN** valid ACWI source data, report, opening state, and FX inputs are supplied
- **THEN** the system returns a GBP dividend result and closing holding artifact

### Requirement: IBKR source assembly
The system SHALL assemble source facts through one configured IBKR source before selecting a dividend method.

#### Scenario: IBKR source assembly
- **WHEN** a tax run supplies its tax year and CG carry-forward state to source assembly
- **THEN** the source assembly pipeline derives an import range, asks its configured IBKR importer to import it, and returns consolidated source facts with non-fatal review issues; the IBKR provider caps its effective end date at the previous New York weekday and hides its 365-day transport limit by retrieving and combining contiguous statement requests

#### Scenario: Future range
- **WHEN** an IBKR import range ends after the previous New York weekday
- **THEN** the provider imports only through that weekday, and rejects a range that begins after it without calling IBKR

#### Scenario: Unavailable Flex statement range
- **WHEN** one IBKR Flex statement request fails with provider error code `1003`
- **THEN** the provider continues importing later adjacent ranges and the source result contains an `INFO` issue with the failed date range and provider code

### Requirement: IBKR-derived security holding snapshot
The system SHALL obtain the IBKR dividend opening holding snapshot from the Open Positions section of an Activity Flex statement ending on 6 October before the tax year.

#### Scenario: Available opening boundary
- **WHEN** the configured Activity Flex template includes Open Positions and IBKR makes the statement ending on 6 October before the tax year available
- **THEN** the importer extracts the account, ISIN, units, timestamp, and source provenance into the security holding snapshot used for the dividend calculation

#### Scenario: Unavailable opening boundary
- **WHEN** IBKR cannot provide the statement ending on 6 October before the tax year
- **THEN** the system records incomplete input for that boundary and does not infer a zero holding

### Requirement: Reviewable ACWI import outcome
The system SHALL distinguish calculated contributions, ignored unrelated activity, review items, and assumptions.

#### Scenario: Missing opening holding
- **WHEN** the permitted opening-zero fallback is used
- **THEN** the import report identifies the security and boundary date as an assumption
