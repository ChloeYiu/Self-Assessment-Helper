package com.helper.ingestion.report.eri;

import com.helper.util.pdf.Pdf;
import java.io.IOException;

/** Retrieves one issuer ERI report as PDF content. */
public interface EriReportProvider {
    /** Retrieves the report whose reporting period ends in the supplied year. */
    Pdf loadReport(int reportingYear) throws IOException;
}
