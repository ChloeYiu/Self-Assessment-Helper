# Self Assessment Helper

A framework-independent Java backend for preparing UK Self Assessment income summaries from broker accounts, bank statements, and manual inputs.

## Design and progress

See the active OpenSpec changes for [ACWI dividends](openspec/changes/acwi-dividend-run/proposal.md), [capital gains](openspec/changes/capital-gains-and-multi-year-state/proposal.md), and [other sources/product access](openspec/changes/other-sources-and-product-access/proposal.md).

The current code includes income calculations, IBKR/ERI parsing, FX services, and shared document models. The complete source-to-summary workflow is still being connected. The task record distinguishes implemented components from partial work and scaffolds.

## Development

The Maven project targets Java 25. Follow [AGENTS.md](AGENTS.md) for repository development instructions.

## Local Configuration

Secrets and account-specific settings should live in local config files that are not committed.

For IBKR Flex Web Service access:

```bash
cp config/ibkr-flex.example.properties config/ibkr-flex.properties
```

Then edit `config/ibkr-flex.properties`:

```properties
ibkr.flex.token=your-flex-web-service-token
ibkr.flex.query-id=your-saved-flex-query-id
ibkr.flex.version=3
```

`ibkr.flex.token` is secret and must not be committed. `ibkr.flex.query-id` is the ID of the saved Flex Query you created in IBKR Client Portal.

The saved IBKR Flex Query should be an XML Activity Flex Query with the `Trades` section enabled. The current IBKR trade table parser expects fields such as `accountId`, `assetCategory`, `symbol`, `description`, `conid`, `transactionID`, `tradeDate`, `settleDateTarget`, `buySell`, `quantity`, `tradePrice`, `proceeds`, `ibCommission`, `currency`, and `fifoPnlRealized`.

Tax-year date ranges should be computed by code from `TaxYear` and sent to IBKR as Flex Web Service request parameters:

```text
fd=yyyyMMdd
td=yyyyMMdd
```

Do not store tax-year dates in the local properties file.

## Building

```bash
mvn clean install
```

## Running Tests

```bash
mvn test
```
