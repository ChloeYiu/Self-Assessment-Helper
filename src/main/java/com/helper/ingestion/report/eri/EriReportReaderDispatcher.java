package com.helper.ingestion.report.eri;

import com.helper.income.config.SecurityConfigEnums.EriReportFormat;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Routes a configured ERI report format to its configured reader. */
public class EriReportReaderDispatcher {
    private final Map<EriReportFormat, EriReportReader> readersByFormat = Map.of(
            EriReportFormat.SSGA_SPDR_UK_REPORTABLE_INCOME,
            new EriReportReader(new SsgaSpdrEriReportProvider(), new SsgaSpdrEriReportExtractor()));

    /** Returns the reader registered for a configured report format, when present. */
    public Optional<EriReportReader> findReader(EriReportFormat reportFormat) {
        return Optional.ofNullable(readersByFormat.get(
                Objects.requireNonNull(reportFormat, "reportFormat")));
    }
}
