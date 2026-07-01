package com.helper.core.income.builder;

import com.helper.core.config.TaxYear;
import java.util.List;

/**
 * Generic builder contract for assembling income aggregates from source items.
 */
public interface IncomeBuilder<TIncome, TSource> {
    TIncome build(TaxYear taxYear, List<TSource> sources);
}
