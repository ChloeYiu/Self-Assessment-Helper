package com.helper.ingestion.report.eri;

import com.helper.income.implementation.dividend.model.AccumulatingFundReport;
import com.helper.util.pdf.Pdf;
import java.io.IOException;

/** Extracts one fund-specific reportable-income result from a retrieved ERI report. */
public interface EriReportExtractor {
    /** Extracts the named fund from one issuer report. */
    AccumulatingFundReport extract(Pdf pdf, String isin, int reportingYear) throws IOException;
}
