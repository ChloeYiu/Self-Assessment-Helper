package com.helper.core.income;

/**
 * Income assembled from source items.
 */
public interface IncomeWithSource<TSource> extends Income {

    /**
     * Add a source item to this income.
     */
    void addSource(TSource source);
}
