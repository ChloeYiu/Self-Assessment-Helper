# Tasks

- [ ] 1 Deliver capital gains and multi-year state.
- [ ] 1.1 Import and normalize the capital-gain window from the shared account basket, including the 30-day extension after tax-year end.
- [ ] 1.2 Connect the Section 104 opening-pool state at 5 April, finalised after the prior year's 30-day extension, consumed-acquisition records, and explicit ERI adjustments; verify that an extension acquisition matched to a prior-year disposal is not pooled again in the new year.
- [ ] 1.3 Route capital-gain contributions into the aggregate/full result.
- [ ] 1.4 Persist and reload the 5 April pool state and consumed-acquisition records after the 30-day extension; verify the next tax-year run can apply the records to its overlapping source trades.

Do not generate tests unless explicitly requested.
