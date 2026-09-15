package com.helper.ingestion.report.eri;

import com.helper.income.implementation.dividend.model.AccumulatingFundReport;
import java.io.IOException;

/** Extracts accumulating fund report data from one issuer ERI report source. */
public interface EriReportExtractor {
    /** Extracts one fund report from the supplied ERI report input. */
    AccumulatingFundReport extractReport(EriReportInput input) throws IOException;
}
