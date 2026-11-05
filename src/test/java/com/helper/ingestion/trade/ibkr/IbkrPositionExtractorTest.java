package com.helper.ingestion.trade.ibkr;

import static org.junit.Assert.assertEquals;

import com.helper.pipeline.importer.Position;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.Test;

public class IbkrPositionExtractorTest {
    @Test
    public void extractPositions_mapsSecurityRowsAndSkipsRowsWithoutAnIsin() {
        List<Position> positions = new IbkrPositionExtractor().extractPositions(
                IbkrTestFixtures.acwiOpenPositionsDocument(),
                LocalDate.of(2025, 10, 6));

        assertEquals(1, positions.size());
        Position position = positions.get(0);
        assertEquals("ibkr-flex", position.source());
        assertEquals("U123", position.accountId());
        assertEquals("IE00B44Z5B48", position.isin());
        assertEquals("ACWI", position.symbol());
        assertEquals(0, new BigDecimal("10").compareTo(position.quantity()));
        assertEquals(LocalDate.of(2025, 10, 6), position.positionDate());
    }
}
