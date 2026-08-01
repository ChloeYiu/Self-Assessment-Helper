# Self Assessment Helper

A flexible, framework-agnostic Java helper for UK Self Assessment workflows. The current focus is preparing gross and adjusted income figures from multiple sources; taxable income and tax due calculations can be added as later extensions.

## Features

- Support for multiple income types:
  - **Rent-a-Room** income with expense tracking
  - **Dividend** income
  - **Savings** income (including foreign savings conversion support)

- Gross and adjusted income summaries by category
- Local income adjustments where the income source has enough context
- FX conversion support for foreign savings
- Detailed summaries for Self Assessment preparation

## Project Structure

```
.github/
└── workflows/
  └── maven.yml

scripts/
└── git-hooks/
  └── pre-commit

src/
├── main/
│   ├── java/com/helper/core/
│   │   ├── calculations/     # Calculation and summary logic
│   │   ├── config/           # Tax-year configuration
│   │   ├── income/           # Income contracts, builders, and implementations
│   │   ├── ingestion/        # Extractors and source entry models
│   │   └── support/          # Shared support services such as FX
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

## Calculation Architecture

The helper separates income facts from later tax decisions:

```text
Input files/manual data
  -> Extractors
  -> Entry objects
  -> Entry-to-income bridge (future)
  -> Income source objects
  -> Income objects
  -> Income results and artifacts
  -> Adjusted income summary/export processing
  -> Allowance calculation (future)
  -> Taxable income calculation (future)
  -> Tax due calculation (future)
```

Current implementation covers the lower layers:

- **Extractors** read source-specific data into entry objects.
- **IncomeWithSource** income objects accept source objects directly.
- **Income objects** produce `IncomeResult` values from `calculateResult()`.
- **IncomeResult** carries gross income, adjusted income, and calculation artifacts.
- **IncomeCalculator** is currently a shell for the future calculation workflow.

The entry-to-income bridge is not implemented yet. When added, it should translate ingested entries into income source objects before those sources are added to income objects.

Simple income categories may be able to feed entries into an income source object incrementally. For example, savings interest entries could add monthly income into a `Saving` source before that source is added to the aggregate `SavingIncome`.

More complex categories can combine multiple source inputs inside the income source object. For example, a non-UK domiciled accumulating dividend source can take broker holding data and an `AccumulatingFundReport`, determine units held on the fund report date, and calculate the resulting dividend amount.

Local adjustments live in the income class when the source has enough context. For example, `RentARoomIncome` can choose between the Rent-a-Room allowance and actual expenses. Most income types use the default adjusted income, which is the same as gross income.

Future layers should use the adjusted income summary as input:

- **Allowance calculation** will determine personal, savings, dividend, and capital gains allowances from the full income picture.
- **TaxableIncomeCalculator** will apply those allowances to calculate taxable income by category.
- **TaxDueCalculator** will apply rates and bands to calculate estimated tax due.

## Income Models

The core income contract is `Income` (`com.helper.core.income.Income`).

Current concrete/related income models include:

- `IncomeCalculator` (`com.helper.core.calculations`) - shell for the future calculation workflow
- `RentARoomIncome` (`com.helper.core.income.implementation`) - Rent-a-Room income with allowance vs actual-expense adjusted-income comparison
- `SavingIncome` (`com.helper.core.income.implementation`) - aggregate savings category income
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
- [ ] Taxable income calculator by category
- [ ] Tax due estimate summaries
