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

### Requirement: IBKR-derived opening holding snapshot
The system SHALL obtain IBKR Open Positions at the shared earliest dividend boundary, 6 October before the tax year, and use the supported ISIN rows as the opening holding baseline for later ERI calculations.

#### Scenario: Available opening boundary
- **WHEN** the configured IBKR Flex Positions query makes the statement ending on 6 October before the tax year available
- **THEN** the importer retains the account-wide statement as source evidence, returns its normalized source positions, and the consolidator creates the opening security holding snapshot for each supported ISIN

#### Scenario: Available statement has no supported security row
- **WHEN** the configured IBKR Flex Positions query returns a valid statement but no row for a supported ISIN
- **THEN** the consolidator creates a zero-quantity opening snapshot for that ISIN and records an `INFO` zero-opening-holding issue

#### Scenario: Unavailable opening boundary
- **WHEN** IBKR cannot provide the statement ending on 6 October before the tax year
- **THEN** the importer returns zero `Position` rows for that request and an `INFO` unavailable-range issue; consolidation creates a zero-quantity holding snapshot and continues

### Requirement: Reviewable ACWI import outcome
The system SHALL distinguish calculated contributions, ignored unrelated activity, review items, and assumptions.

#### Scenario: Missing opening holding
- **WHEN** no opening holding snapshot was created by source consolidation
- **THEN** the non-UK accumulating-fund pipeline returns an `ERROR` issue for that ISIN and does not infer a zero holding or prepare its dividend source

### Requirement: Non-UK accumulating-fund preparation
The system SHALL prepare one non-UK accumulating-fund dividend source from the configured ERI format, the report applicable to the requested tax year, the opening holding snapshot, post-boundary report-period trades, and required FX rates.

#### Scenario: Valid ERI preparation
- **WHEN** a configured security has a registered ERI extractor, a valid report for the tax-year start year, exactly one 6 October opening snapshot, valid later trades through the report end, and monthly or yearly HMRC FX rates
- **THEN** the system returns a prepared `NonUkDomicileAccumulatingDividend` source and does not return an issue for that security

#### Scenario: Preparation prerequisite unavailable
- **WHEN** the ERI format has no registered extractor, the report cannot be loaded or parsed, the boundary snapshot is missing or invalid, the FX input is unavailable, or the prepared source is invalid
- **THEN** the system returns an `ERROR` issue with the ISIN and skips that security while continuing with other configured securities
