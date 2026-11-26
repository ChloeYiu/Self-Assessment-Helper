# Spec Delta

## Purpose

Calculate reviewable capital-gain results from normalized taxable transactions while retaining state needed by later tax years.

## ADDED Requirements

### Requirement: Correct capital-gain input window
The system SHALL include transactions from tax-year start through tax-year end plus 30 days when preparing capital-gain inputs.

#### Scenario: 30-day acquisition window
- **WHEN** a disposal occurs near tax-year end and a relevant acquisition follows within 30 days
- **THEN** the acquisition is available to the capital-gain calculation

### Requirement: Reusable capital-gain state
The system SHALL return closing carry-forward state with enough information for a later tax-year run.

#### Scenario: Closing carry-forward
- **WHEN** a capital-gain calculation completes
- **THEN** the result retains pool cost, quantity, and consumed acquisitions for future use

#### Scenario: Prior-year 30-day acquisition in the next tax year
- **WHEN** an acquisition after tax-year end is matched to a disposal in the prior tax year
- **THEN** the next tax-year calculation imports that acquisition and excludes its consumed quantity and cost from its Section 104 pool
