package com.helper.ingestion.report.eri;

import com.helper.income.implementation.dividend.model.AccumulatingFundReport;

/**
 * Manual live flow that prints ACWI data extracted from SSGA's published ERI report.
 *
 * <p>Run with {@code mvn -Dexec.mainClass=com.helper.ingestion.report.eri.
 * SsgaSpdrEriReportExtractorFlow -Dexec.classpathScope=test
 * org.codehaus.mojo:exec-maven-plugin:3.6.4:java}. Pass one optional reporting year; the
 * default is 2025.</p>
 */
public class SsgaSpdrEriReportExtractorFlow {
    private static final String ACWI_ISIN = "IE00B44Z5B48";

    public static void main(String[] args) throws Exception {
        int reportingYear = parseReportingYear(args);
        AccumulatingFundReport report = new SsgaSpdrEriReportExtractor()
                .extractReport(ACWI_ISIN, reportingYear);

        printReport(report);
    }

    private static int parseReportingYear(String[] args) {
        if (args.length == 0) {
            return 2025;
        }
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: SsgaSpdrEriReportExtractorFlow [reporting-year]");
        }
        return Integer.parseInt(args[0]);
    }

    private static void printReport(AccumulatingFundReport report) {
        System.out.println("fundIdentifier=" + report.getFundIdentifier());
        System.out.println("reportingPeriodStartDate=" + report.getReportingPeriodStartDate());
        System.out.println("reportingPeriodEndDate=" + report.getReportingPeriodEndDate());
        System.out.println("fundDistributionDate=" + report.getFundDistributionDate());
        System.out.println("reportedIncomePerUnit=" + report.getReportedIncomePerUnit());
        System.out.println("currencyCode=" + report.getCurrencyCode());
    }
}
