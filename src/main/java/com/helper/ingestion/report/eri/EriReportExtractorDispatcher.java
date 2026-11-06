package com.helper.ingestion.report.eri;

import com.helper.income.config.SecurityConfigEnums.EriReportFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Routes a configured ERI report format to the extractor that supports it. */
public class EriReportExtractorDispatcher {
    private final Map<EriReportFormat, EriReportExtractor> extractorsByFormat = Map.of(
            EriReportFormat.SSGA_SPDR_UK_REPORTABLE_INCOME,
            new SsgaSpdrEriReportExtractor());

    /** Returns the extractor registered for a configured report format, when present. */
    public Optional<EriReportExtractor> findExtractor(EriReportFormat reportFormat) {
        return Optional.ofNullable(extractorsByFormat.get(
                Objects.requireNonNull(reportFormat, "reportFormat")));
    }
}
