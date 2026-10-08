package com.helper.ingestion.trade.ibkr;

import static org.junit.Assert.assertEquals;

import com.helper.config.CurrencyCode;
import com.helper.income.implementation.Security;
import java.util.List;
import org.junit.Test;

public class IbkrSecurityExtractorTest {
    @Test
    public void extractSecurities_mapsSecuritiesTableToDomainSecurities() {
        List<Security> securities = new IbkrSecurityExtractor().extractSecurities(
                IbkrTestFixtures.acwiBuyDocument());

        assertEquals(1, securities.size());
        Security security = securities.get(0);
        assertEquals("IE00B44Z5B48", security.getIdentifier());
        assertEquals("ACWI", security.getTicker());
        assertEquals("SPDR MSCI ACWI UCITS ETF", security.getName());
        assertEquals(CurrencyCode.USD, security.getCurrencyCode());
    }
}
