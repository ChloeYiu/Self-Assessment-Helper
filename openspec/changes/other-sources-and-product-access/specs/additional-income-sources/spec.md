# Spec Delta

## Purpose

Prepare supported savings and manual income inputs from source documents while keeping ordinary transactions reviewable and separate from tax calculation.

## ADDED Requirements

### Requirement: Supported additional sources
The system SHALL accept supported bank, statement, and manual sources without requiring callers to classify every input row into a tax category.

#### Scenario: Supported savings input
- **WHEN** a supported statement contains a strong interest match
- **THEN** the system produces a savings input and reports unrelated activity compactly
