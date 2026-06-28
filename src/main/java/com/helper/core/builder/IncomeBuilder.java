package com.helper.core.builder;

import java.util.List;

/**
 * Generic builder contract for assembling income aggregates from source items.
 */
public interface IncomeBuilder<TIncome, TSource> {
    TIncome build(int taxYear, List<TSource> sources);
}
