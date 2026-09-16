package com.helper.ingestion.report.eri;

import com.helper.config.CurrencyCode;
import com.helper.util.pdf.PdfTextDocument;
import com.helper.util.pdf.PdfTextFilter;
import com.helper.income.implementation.dividend.model.AccumulatingFundReport;
import java.math.BigDecimal;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extracts SSGA SPDR UK reportable income ERI PDF reports. */
public class SsgaSpdrEriReportExtractor implements EriReportExtractor {
    private static final DateTimeFormatter REPORT_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("d MMMM uuuu", Locale.ENGLISH);
    private static final Pattern REPORTING_PERIOD_ENDED_PATTERN = Pattern.compile(
            "(?i).*reporting\\s+period\\s+ended\\s+(\\d{1,2}\\s+[A-Za-z]+\\s+\\d{4}).*"
    );
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{1,2}\\s+[A-Za-z]+\\s+\\d{4})");
    private static final Pattern ROW_VALUE_PATTERN = Pattern.compile(
            "^.*?%s\\s+\\S+\\s+([A-Z]{3})\\s+([0-9]+(?:\\.[0-9]+)?)(?:\\s+|$)(.*)$"
    );

    /** Creates a deterministic ERI report extractor. */
    public SsgaSpdrEriReportExtractor() {
    }

    /**
     * Extracts the accumulating fund report row from a shared ERI report input.
     */
    @Override
    public AccumulatingFundReport extractReport(EriReportInput input) throws IOException {
        EriReportInput reportInput = Objects.requireNonNull(input, "input");
        String fundIdentifier = requireNonBlank(reportInput.getIsin(), "isin");
        PdfTextDocument document = reportInput.loadPdfTextDocument();
        LocalDate reportingPeriodEndDate = extractReportingPeriodEndDate(document, reportInput.getReportingYear());
        EriReportRow row = extractReportRow(document, fundIdentifier);

        LocalDate expectedDistributionDate = reportingPeriodEndDate.plusMonths(6);
        if (!row.fundDistributionDate().equals(expectedDistributionDate)) {
            throw new IllegalArgumentException("fund distribution date must be six months after reporting period end date");
        }

        return new AccumulatingFundReport(
                fundIdentifier,
                reportingPeriodEndDate.minusYears(1).plusDays(1),
                reportingPeriodEndDate,
                row.fundDistributionDate(),
                row.reportedIncomePerUnit(),
                row.currencyCode()
        );
    }

    private LocalDate extractReportingPeriodEndDate(PdfTextDocument document, int reportingYear) {
        return PdfTextFilter.matchingText(document, text -> REPORTING_PERIOD_ENDED_PATTERN.matcher(text).matches())
                .stream()
                .map(line -> parseReportingPeriodEndDate(line.getText()))
                .filter(date -> date.getYear() == reportingYear)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "reporting period end date was not found for reporting year " + reportingYear
                ));
    }

    private LocalDate parseReportingPeriodEndDate(String text) {
        Matcher matcher = REPORTING_PERIOD_ENDED_PATTERN.matcher(text);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("reporting period end date line is invalid");
        }
        return parseReportDate(matcher.group(1));
    }

    private EriReportRow extractReportRow(PdfTextDocument document, String isin) {
        return PdfTextFilter.matchingText(document, text -> text.contains(isin))
                .stream()
                .map(line -> tryParseReportRow(line.getText(), isin))
                .flatMap(Optional::stream)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "ERI row was not found or did not match the expected SSGA report format for ISIN " + isin
                ));
    }

    private Optional<EriReportRow> tryParseReportRow(String rowText, String isin) {
        Pattern rowPattern = Pattern.compile(String.format(ROW_VALUE_PATTERN.pattern(), Pattern.quote(isin)));
        Matcher matcher = rowPattern.matcher(rowText);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        CurrencyCode currencyCode = CurrencyCode.parse(matcher.group(1), "ERI report");
        BigDecimal reportedIncomePerUnit = new BigDecimal(matcher.group(2));
        LocalDate fundDistributionDate = extractFirstDate(matcher.group(3));

        return Optional.of(new EriReportRow(currencyCode, reportedIncomePerUnit, fundDistributionDate));
    }

    private LocalDate extractFirstDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (!matcher.find()) {
            throw new IllegalArgumentException("ERI row did not include a fund distribution date");
        }
        return parseReportDate(matcher.group(1));
    }

    private LocalDate parseReportDate(String text) {
        try {
            return LocalDate.parse(text, REPORT_DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("invalid report date: " + text, exception);
        }
    }

    private static String requireNonBlank(String value, String name) {
        String result = Objects.requireNonNull(value, name);
        if (result.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return result;
    }

    private record EriReportRow(
            CurrencyCode currencyCode,
            BigDecimal reportedIncomePerUnit,
            LocalDate fundDistributionDate
    ) {
    }
}
