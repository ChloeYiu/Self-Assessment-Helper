package com.helper.ingestion.trade.ibkr;

import com.helper.pipeline.importer.Position;
import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularRow;
import com.helper.util.table.TabularTable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Extracts security positions from an IBKR Flex Open Positions statement. */
public class IbkrPositionExtractor {
    private static final String SOURCE = "ibkr-flex";

    /**
     * Extracts security positions reported at the requested cutoff date.
     *
     * <p>Rows without an ISIN are non-security positions and are ignored. Every returned row must
     * contain account, ISIN, symbol, quantity, and matching report date values.</p>
     *
     * @param document mapped IBKR Flex document containing an OpenPositions table
     * @param cutoffDate expected Open Positions report date
     * @return normalized source positions for security rows
     */
    public List<Position> extractPositions(TabularDocument document, LocalDate cutoffDate) {
        Objects.requireNonNull(cutoffDate, "cutoffDate");
        TabularTable positionsTable = Objects.requireNonNull(document, "document")
                .getTable(IbkrNames.Tables.OPEN_POSITIONS)
                .orElseThrow(() -> new IllegalArgumentException(
                        "document must contain " + IbkrNames.Tables.OPEN_POSITIONS + " table"));
        return positionsTable.getRows().stream()
                .filter(this::isSecurityPosition)
                .map(row -> extractPosition(row, cutoffDate))
                .toList();
    }

    private boolean isSecurityPosition(TabularRow row) {
        return row.getCell(IbkrNames.OpenPositionColumns.ISIN)
                .flatMap(cell -> cell.getNormalizedText())
                .filter(value -> !value.isBlank())
                .isPresent();
    }

    private static Position extractPosition(TabularRow row, LocalDate expectedCutoffDate) {
        LocalDate positionDate = row.getLocalDate(IbkrNames.OpenPositionColumns.REPORT_DATE);
        if (!positionDate.equals(expectedCutoffDate)) {
            throw new IllegalArgumentException(
                    "IBKR Open Position reportDate must match cutoff date: " + expectedCutoffDate);
        }
        return new Position(
                SOURCE,
                row.getText(IbkrNames.OpenPositionColumns.ACCOUNT_ID),
                row.getText(IbkrNames.OpenPositionColumns.ISIN),
                row.getText(IbkrNames.OpenPositionColumns.SYMBOL),
                row.getBigDecimal(IbkrNames.OpenPositionColumns.POSITION),
                positionDate);
    }
}
