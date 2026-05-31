# Tax Calculator

A flexible, framework-agnostic Java backend for calculating UK personal income tax from multiple income sources.

## Features

- Support for multiple income types:
  - **Rent-a-Room** income with expense tracking
  - **Dividend** income
  - **Savings** income (including foreign savings conversion support)

- Accurate UK tax calculations for 2024/25 tax year
- Income allowances and deductions per income type
- Tax bracket calculations (basic, higher, additional rates)
- Detailed tax summaries

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
│   ├── java/com/taxcalc/
│   │   ├── builder/          # Builder utilities
│   │   ├── calculations/     # Tax calculation logic
│   │   ├── config/           # Tax-year and expense configuration
│   │   ├── fx/               # FX rate contracts and services
│   │   └── income/           # Income contracts and implementations
│   └── resources/
│       └── tax/
│           └── tax-allowances.json
└── test/
  └── java/com/taxcalc/
    ├── calculations/
    └── income/

pom.xml
setup.sh
```

## Income Models

The core income contract is `Income` (`com.taxcalc.income.Income`).

Current concrete/related income models include:

- `RentARoomIncome` (`com.taxcalc.income.implementation`) - Rent-a-Room income with allowance vs actual-expense comparison
- `SavingIncome` (`com.taxcalc.income.implementation`) - aggregate savings category income (applies savings allowance)
- `ForeignSaving` (`com.taxcalc.income.implementation.savings`) - foreign savings input with monthly/yearly FX conversion support
- `Saving` (`com.taxcalc.income.implementation.savings`) - contract for savings contributors used by `SavingIncome`
- `Dividend` (`com.taxcalc.income.implementation.dividend`) - contract for dividend contributors

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

- [ ] National Insurance calculations
- [ ] Capital Gains Tax calculations
- [ ] Self-employment income and NI
- [ ] Marriage Allowance calculations
- [ ] Child benefit tax charges
- [ ] Student Loans repayment tracking
- [ ] Export to tax software formats (CSV, JSON)
- [ ] Web UI integration
- [ ] CLI tool wrapper
