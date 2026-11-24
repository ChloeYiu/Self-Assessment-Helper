package com.helper.ingestion.report.eri;

import com.helper.util.pdf.Pdf;
import java.io.IOException;
import java.net.URI;

/** Retrieves SSGA SPDR Europe I reportable-income PDFs. */
public final class SsgaSpdrEriReportProvider implements EriReportProvider {
    private static final String REPORT_URI_PREFIX = "https://www.ssga.com/library-content/announcement/"
            + "etfs/emea/uk-reportable-income/";
    private static final int EARLIEST_SUPPORTED_REPORTING_YEAR = 2023;
    private static final int LATEST_SUPPORTED_REPORTING_YEAR = 2025;


    /** Retrieves one supported SSGA report as PDF content. */
    @Override
    public Pdf loadReport(int reportingYear) throws IOException {
        return new Pdf(resolveReportUri(reportingYear));
    }

    URI resolveReportUri(int reportingYear) {
        if (reportingYear < EARLIEST_SUPPORTED_REPORTING_YEAR
                || reportingYear > LATEST_SUPPORTED_REPORTING_YEAR) {
            throw new IllegalArgumentException("SSGA Europe I report year is not supported: " + reportingYear);
        }
        if (reportingYear == 2023) {
            return URI.create(REPORT_URI_PREFIX
                    + "revised-ssga-spdr-i-excess-reportable-income-report-as-at-31-march-2023.pdf");
        }
        return URI.create(REPORT_URI_PREFIX
                + "ssga-spdr-etfs-europe-i-plc-reportable-income-report-as-at-31-march-"
                + reportingYear + ".pdf");
    }
}
