# Self Assessment Helper

A flexible, framework-agnostic Java helper for UK Self Assessment workflows. The current focus is preparing gross income figures from multiple sources; taxable amount and tax estimate features can be added as later extensions.

## Features

- Support for multiple income types:
  - **Rent-a-Room** income with expense tracking
  - **Dividend** income
  - **Savings** income (including foreign savings conversion support)

- Gross income summaries by category
- Allowance and taxable amount helpers where useful
- FX conversion support for foreign savings
- Detailed summaries for Self Assessment preparation

## Project Structure

```
.github/
├── copilot-instructions.md
└── workflows/
  └── maven.yml

scripts/
└── git-hooks/
  └── pre-commit

src/
├── main/
│   ├── java/com/helper/core/
│   │   ├── builder/          # Builder utilities
│   │   ├── calculations/     # Calculation and summary logic
│   │   ├── config/           # Tax-year and expense configuration
│   │   ├── fx/               # FX rate contracts and services
│   │   └── income/           # Income contracts and implementations
│   └── resources/
│       └── tax/
│           └── tax-allowances.json
└── test/
  └── java/com/helper/core/
    ├── calculations/
    └── income/

pom.xml
setup.sh
```

## Income Models

The core income contract is `Income` (`com.helper.core.income.Income`).

Current concrete/related income models include:

- `Calculator` (`com.helper.core.calculations`) - calculation entry point, currently including gross income summaries by category
- `RentARoomIncome` (`com.helper.core.income.implementation`) - Rent-a-Room income with allowance vs actual-expense comparison
- `SavingIncome` (`com.helper.core.income.implementation`) - aggregate savings category income, with taxable amount estimation support
- `ForeignSaving` (`com.helper.core.income.implementation.savings`) - foreign savings input with monthly/yearly FX conversion support
- `Saving` (`com.helper.core.income.implementation.savings`) - contract for savings contributors used by `SavingIncome`
- `Dividend` (`com.helper.core.income.implementation.dividend`) - contract for dividend contributors

## Usage

TODO: Add up-to-date usage examples after API stabilization.

## Building

```bash
mvn clean install
```

## Running Tests

```bash
mvn test
```

## Future Enhancements

- [ ] Capital gains income summaries
- [ ] Self-employment income and NI
- [ ] Marriage Allowance calculations
- [ ] Child benefit tax charges
- [ ] Student Loans repayment tracking
- [ ] Export to Self Assessment helper formats (CSV, JSON)
- [ ] Web UI integration
- [ ] CLI tool wrapper
- [ ] Taxable amount estimates by category
- [ ] Tax due estimate summaries
