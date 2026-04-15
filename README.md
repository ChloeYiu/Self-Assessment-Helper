# Tax Calculator

A flexible, framework-agnostic Java backend for calculating UK personal income tax from multiple income sources.

## Features

- Support for multiple income types:
  - **Rent-a-Room** income with expense tracking
  - **Foreign income** with tax credit calculations
  - **Local savings interest** (ISAs, savings accounts, bonds)
  - **ETF investments** with dividend and capital gains tracking
  - **Managed investments** (Nutmeg, robo-advisors, etc.)

- Accurate UK tax calculations for 2024/25 tax year
- Income allowances and deductions per income type
- Tax bracket calculations (basic, higher, additional rates)
- Detailed tax summaries

## Project Structure

```
src/
├── main/
│   ├── java/com/taxcalc/
│   │   ├── models/           # Income source models
│   │   ├── calculations/     # Tax calculation logic
│   │   └── config/           # Configuration
│   └── resources/            # Configuration files
└── test/                     # Unit tests
```

## Income Models

Each income source implements the `IncomeSource` interface:

- `RentARoomIncome` - Rent-a-Room scheme income
- `ForeignIncome` - Foreign employment/investment income
- `LocalSavingsInterest` - Interest from savings/bonds
- `ETFInvestment` - ETF dividend income
- `ManagedInvestment` - Managed account returns (Nutmeg, etc.)

## Usage

```java
// Create tax allowance for the current year
TaxAllowance allowance = new TaxAllowance(2025);

// Create income sources
List<IncomeSource> sources = new ArrayList<>();
sources.add(new RentARoomIncome(new BigDecimal("15000"), 2025));
sources.add(new LocalSavingsInterest(new BigDecimal("250"), "ISA", "Barclays", 2025));
sources.add(new ETFInvestment(new BigDecimal("350"), "VWRL", "Interactive Brokers", 2025));

// Calculate taxes
TaxCalculator calculator = new TaxCalculator(allowance);
TaxCalculator.TaxSummary summary = calculator.calculateTaxSummary(sources);

System.out.println(summary);
```

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

## Tax Year Support

Currently supports 2024/25 tax year. Update `TaxAllowance.java` to add support for other years.

## Disclaimer

This calculator is provided as-is for informational purposes. Always verify calculations with official HMRC guidance or consult a tax professional before submitting tax returns.
