package com.helper.income.implementation;

import com.helper.config.TaxYear;
import com.helper.income.IncomeResult;
import com.helper.income.IncomeType;
import com.helper.income.IncomeWithSource;
import com.helper.income.artifact.IncomeArtifact;
import com.helper.income.implementation.capitalgain.CapitalGain;
import com.helper.income.implementation.capitalgain.model.CapitalGainResult;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate for capital gain income sources.
 */
public class CapitalGainIncome implements IncomeWithSource<CapitalGain> {
    private final TaxYear taxYear;
    private final List<CapitalGain> capitalGainSources;

    public CapitalGainIncome(TaxYear taxYear) {
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
        this.capitalGainSources = new ArrayList<>();
    }

    @Override
    public void addSource(CapitalGain source) {
        CapitalGain value = Objects.requireNonNull(source, "source");
        if (!value.getTaxYear().equals(taxYear)) {
            throw new IllegalArgumentException("source tax year must match aggregate tax year");
        }
        capitalGainSources.add(value);
    }

    @Override
    public IncomeType getIncomeType() {
        return IncomeType.CAPITAL_GAIN;
    }

    @Override
    public IncomeResult calculateResult() {
        BigDecimal capitalGainAmount = BigDecimal.ZERO;
        List<IncomeArtifact> artifacts = new ArrayList<>();
        for (CapitalGain capitalGainSource : capitalGainSources) {
            CapitalGainResult result = capitalGainSource.calculateCapitalGain();
            capitalGainAmount = capitalGainAmount.add(result.getCapitalGainAmount());
            artifacts.addAll(result.getArtifacts());
        }
        return new IncomeResult(getIncomeType(), capitalGainAmount, capitalGainAmount, artifacts);
    }

    @Override
    public TaxYear getTaxYear() {
        return taxYear;
    }
}
