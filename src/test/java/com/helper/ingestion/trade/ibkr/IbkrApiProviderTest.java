package com.helper.ingestion.trade.ibkr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

import com.helper.util.api.MockHttpClient;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import org.junit.Test;

public class IbkrApiProviderTest {

    @Test
    public void getFlexStatementsXml_usesReferenceCodeToFetchStatementXml() throws Exception {
        MockHttpClient httpClient = new MockHttpClient()
                .respondWith(200,
                        "<FlexStatementResponse><Status>Success</Status><ReferenceCode>abc-123</ReferenceCode>"
                                + "</FlexStatementResponse>")
                .respondWith(200, "<FlexQueryResponse><FlexStatements count=\"1\" /></FlexQueryResponse>");

        IbkrApiProvider provider = new IbkrApiProvider(httpClient, createProperties());

        List<String> statementXmls = provider.getFlexStatementsXml(
                LocalDate.of(2025, 4, 6),
                LocalDate.of(2026, 4, 5));

        assertEquals(
                List.of("<FlexQueryResponse><FlexStatements count=\"1\" /></FlexQueryResponse>"),
                statementXmls);

        URI referenceCodeUri = httpClient.getRequestedUris().get(0);
        URI statementUri = httpClient.getRequestedUris().get(1);
        assertTrue(referenceCodeUri.toString().contains("/SendRequest"));
        assertTrue(referenceCodeUri.toString().contains("q=query-id"));
        assertTrue(referenceCodeUri.toString().contains("fd=20250406"));
        assertTrue(referenceCodeUri.toString().contains("td=20260405"));
        assertTrue(statementUri.toString().contains("/GetStatement"));
        assertTrue(statementUri.toString().contains("q=abc-123"));
    }

    @Test
    public void getFlexStatementsXml_splitsRangesLongerThan365Days() throws Exception {
        MockHttpClient httpClient = new MockHttpClient()
                .respondWith(200,
                        "<FlexStatementResponse><Status>Success</Status><ReferenceCode>first</ReferenceCode>"
                                + "</FlexStatementResponse>")
                .respondWith(200, "<FlexQueryResponse><FlexStatements count=\"1\" /></FlexQueryResponse>")
                .respondWith(200,
                        "<FlexStatementResponse><Status>Success</Status><ReferenceCode>second</ReferenceCode>"
                                + "</FlexStatementResponse>")
                .respondWith(200, "<FlexQueryResponse><FlexStatements count=\"1\" /></FlexQueryResponse>");
        IbkrApiProvider provider = new IbkrApiProvider(httpClient, createProperties());

        List<String> statementXmls = provider.getFlexStatementsXml(
                LocalDate.of(2023, 10, 6),
                LocalDate.of(2025, 5, 5));

        assertEquals(2, statementXmls.size());
        assertTrue(httpClient.getRequestedUris().get(0).toString().contains("fd=20231006"));
        assertTrue(httpClient.getRequestedUris().get(0).toString().contains("td=20241004"));
        assertTrue(httpClient.getRequestedUris().get(2).toString().contains("fd=20241005"));
        assertTrue(httpClient.getRequestedUris().get(2).toString().contains("td=20250505"));
    }

    @Test
    public void getFlexStatementsXml_throwsWhenIbkrReturnsFailureStatus() throws Exception {
        MockHttpClient httpClient = new MockHttpClient()
                .respondWith(200,
                        "<FlexStatementResponse><Status>Fail</Status><ErrorCode>101</ErrorCode>"
                                + "<ErrorMessage>Bad request</ErrorMessage></FlexStatementResponse>");

        IbkrApiProvider provider = new IbkrApiProvider(httpClient, createProperties());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.getFlexStatementsXml(LocalDate.of(2025, 4, 6), LocalDate.of(2025, 4, 7)));

        assertEquals("IBKR Flex SendRequest failed: error code 101 - Bad request", exception.getMessage());
    }

    private static Properties createProperties() {
        Properties properties = new Properties();
        properties.setProperty("ibkr.flex.token", "test-token");
        properties.setProperty("ibkr.flex.query-id", "query-id");
        properties.setProperty("ibkr.flex.version", "3");
        return properties;
    }
}
