package com.helper.ingestion.report.eri;

import com.helper.income.implementation.dividend.model.AccumulatingFundReport;
import com.helper.util.pdf.Pdf;
import java.io.IOException;
import java.util.Objects;

/** Reads one ERI report through its provider and extractor. */
public final class EriReportReader {
    private final EriReportProvider provider;
    private final EriReportExtractor extractor;

    /** Creates an ERI reader from one report provider and extractor. */
    public EriReportReader(EriReportProvider provider, EriReportExtractor extractor) {
        this.provider = Objects.requireNonNull(provider, "provider");
        this.extractor = Objects.requireNonNull(extractor, "extractor");
    }

    /** Reads one fund's ERI report data. */
    public AccumulatingFundReport readReport(String isin, int reportingYear) throws IOException {
        if (Objects.requireNonNull(isin, "isin").isBlank()) {
            throw new IllegalArgumentException("isin must not be blank");
        }
        Pdf pdf = provider.loadReport(reportingYear);
        return extractor.extract(pdf, isin, reportingYear);
    }
}
