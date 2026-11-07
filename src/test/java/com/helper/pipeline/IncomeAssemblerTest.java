package com.helper.pipeline;

import static org.junit.Assert.assertEquals;

import com.helper.config.TaxYear;
import com.helper.income.IncomeType;
import com.helper.income.implementation.dividend.Dividend;
import com.helper.income.implementation.dividend.model.DividendResult;
import com.helper.issue.Issue;
import com.helper.issue.IssueCode;
import com.helper.issue.IssueSeverity;
import com.helper.pipeline.investment.InvestmentPipelineResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class IncomeAssemblerTest {
    @Test
    public void add_investmentResultAddsDividendSourcesAndIssuesToTheTaxCalculationResult() {
        TaxYear taxYear = TaxYear.of(2025);
        Issue issue = new Issue(
                IssueCode.UNIMPLEMENTED_DIVIDEND_METHOD,
                IssueSeverity.WARNING,
                "test",
                Map.of("isin", "IE00B44Z5B48"));
        IncomeAssembler assembler = new IncomeAssembler(taxYear);

        assembler.add(new InvestmentPipelineResult(
                List.of(new FixedDividend(taxYear, new BigDecimal("12.34"))),
                List.of(issue)));

        assertEquals(
                new BigDecimal("12.34"),
                assembler.build().findIncomeResult(IncomeType.DIVIDENDS).orElseThrow().getGrossIncome());
        assertEquals(List.of(issue), assembler.build().getIssues());
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_unregisteredResultTypeRejectsIt() {
        new IncomeAssembler(TaxYear.of(2025)).add(() -> List.of());
    }

    private record FixedDividend(TaxYear taxYear, BigDecimal amount) implements Dividend {
        @Override
        public DividendResult calculateDividendResult() {
            return new DividendResult(amount);
        }

        @Override
        public TaxYear getTaxYear() {
            return taxYear;
        }
    }
}
