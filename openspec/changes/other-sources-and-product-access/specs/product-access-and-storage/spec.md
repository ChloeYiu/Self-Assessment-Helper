# Spec Delta

## Purpose

Provide future access to calculated results and preserve the design constraints for reusable storage and amendments.

## ADDED Requirements

### Requirement: Explicit source pipeline selection
The system SHALL select an available source pipeline explicitly when multiple source types are supported.

#### Scenario: Multiple source types
- **WHEN** a caller selects a supported source type
- **THEN** the system routes the request to its matching available pipeline
