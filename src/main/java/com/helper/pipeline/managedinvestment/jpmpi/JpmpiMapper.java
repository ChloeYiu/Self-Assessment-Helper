package com.helper.pipeline.managedinvestment.jpmpi;

import com.helper.config.TaxYear;
import com.helper.income.IncomeType;
import com.helper.issue.Issue;
import com.helper.issue.IssueSeverity;
import com.helper.util.pdf.PdfTextDocument;
import com.helper.util.pdf.PdfTextLine;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps provider-reported totals from the known JPMPI annual tax-pack text format.
 *
 * <p>The caller supplies already-extracted PDF text. This class does not read files or invoke
 * PDFBox itself.</p>
 */
public final class JpmpiMapper {
    private static final String PROVIDER_NAME = "J.P. Morgan Personal Investing";
    private static final String ISSUE_SOURCE = "jpmpi-source";
    private static final Pattern TAX_YEAR_PATTERN = Pattern.compile("(\\d{4})/\\d{2} tax year");
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("Account ([A-Z0-9]+)");
    private static final Pattern FOREIGN_INTEREST_PATTERN = Pattern.compile(
            "^Foreign interest\\s+[A-Z]{3}\\s+([0-9,]+\\.\\d{2})");
    private static final Pattern FOREIGN_DIVIDEND_PATTERN = Pattern.compile(
            "^Foreign dividend\\s+[A-Z]{3}\\s+([0-9,]+\\.\\d{2})");
    private static final Pattern UK_SAVINGS_PATTERN = Pattern.compile(
            "^UK bank interest untaxed\\s+([0-9,]+\\.\\d{2})");
    private static final Pattern CAPITAL_LOSS_PATTERN = Pattern.compile(
            "capital loss of \\(?£?([0-9,]+\\.\\d{2})\\)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern CAPITAL_GAIN_PATTERN = Pattern.compile(
            "capital gain of \\(?£?([0-9,]+\\.\\d{2})\\)?", Pattern.CASE_INSENSITIVE);

    /**
     * Maps ordered PDF text into one JPMPI tax-pack result.
     *
     * <p>The implementation verifies the pack identity and tax year, parses printed GBP totals
     * by {@code IncomeType}, and reports a recoverable issue when no totals are present.</p>
     *
     * @param document ordered text extracted from one selected PDF
     * @return provider-reported result for that tax pack
     */
    public JpmpiImportResult map(PdfTextDocument document) {
        List<PdfTextLine> summaryLines = summaryLines(Objects.requireNonNull(document, "document"));
        String accountId = findRequired(ACCOUNT_PATTERN, summaryLines, "account identifier");
        TaxYear taxYear = TaxYear.of(Integer.parseInt(findRequired(TAX_YEAR_PATTERN, summaryLines, "tax year")));
        Map<IncomeType, BigDecimal> reportedAmounts = new EnumMap<>(IncomeType.class);

        addSummaryAmounts(summaryLines, FOREIGN_INTEREST_PATTERN, IncomeType.FOREIGN_SAVINGS, reportedAmounts);
        addSummaryAmounts(summaryLines, FOREIGN_DIVIDEND_PATTERN, IncomeType.FOREIGN_DIVIDENDS, reportedAmounts);
        addSummaryAmounts(summaryLines, UK_SAVINGS_PATTERN, IncomeType.UK_SAVINGS, reportedAmounts);
        addCapitalGainOrLoss(summaryLines, reportedAmounts);

        List<Issue> issues = new ArrayList<>();
        if (reportedAmounts.isEmpty()) {
            issues.add(new Issue(
                    JpmpiIssueCode.SOURCE_TOTALS_UNAVAILABLE,
                    IssueSeverity.ERROR,
                    ISSUE_SOURCE,
                    Map.of("fileName", document.getSourceName().orElse("unknown"))));
        }
        JpmpiImportResult result = new JpmpiImportResult(
                PROVIDER_NAME,
                accountId,
                taxYear,
                document.getSourceName().orElse("unknown"),
                issues);
        reportedAmounts.forEach(result::addReportedAmount);
        return result;
    }

    // Summary-page selection: page 2 prints each total once; later schedules repeat the values.
    private static List<PdfTextLine> summaryLines(PdfTextDocument document) {
        List<PdfTextLine> pageTwoLines = document.getLines().stream()
                .filter(line -> line.getLocation().map(location -> location.getPageNumber() == 2).orElse(false))
                .toList();
        return pageTwoLines.isEmpty() ? document.getLines() : pageTwoLines;
    }

    // Pack identity: account and tax year must be present before this PDF is accepted as JPMPI.
    private static String findRequired(Pattern pattern, List<PdfTextLine> lines, String fieldName) {
        return lines.stream()
                .map(PdfTextLine::getText)
                .map(pattern::matcher)
                .filter(Matcher::find)
                .map(matcher -> matcher.group(1))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("JPMPI tax pack is missing " + fieldName));
    }

    // Reported totals: foreign rows can appear once per country, so matching amounts are summed.
    private static void addSummaryAmounts(
            List<PdfTextLine> lines,
            Pattern pattern,
            IncomeType incomeType,
            Map<IncomeType, BigDecimal> reportedAmounts) {
        lines.stream()
                .map(PdfTextLine::getText)
                .map(pattern::matcher)
                .filter(Matcher::find)
                .map(matcher -> amount(matcher.group(1)))
                .reduce(BigDecimal::add)
                .ifPresent(amount -> reportedAmounts.put(incomeType, amount));
    }

    private static void addCapitalGainOrLoss(
            List<PdfTextLine> lines,
            Map<IncomeType, BigDecimal> reportedAmounts) {
        findAmount(CAPITAL_LOSS_PATTERN, lines)
                .map(BigDecimal::negate)
                .or(() -> findAmount(CAPITAL_GAIN_PATTERN, lines))
                .ifPresent(amount -> reportedAmounts.put(IncomeType.CAPITAL_GAIN, amount));
    }

    private static Optional<BigDecimal> findAmount(Pattern pattern, List<PdfTextLine> lines) {
        return lines.stream()
                .map(PdfTextLine::getText)
                .map(pattern::matcher)
                .filter(Matcher::find)
                .map(matcher -> amount(matcher.group(1)))
                .findFirst();
    }

    private static BigDecimal amount(String text) {
        return new BigDecimal(text.replace(",", ""));
    }
}
