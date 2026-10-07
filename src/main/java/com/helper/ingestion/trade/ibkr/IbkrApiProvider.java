package com.helper.ingestion.trade.ibkr;

import com.helper.util.api.ApiRequest;
import com.helper.util.api.DateRangeValidator;
import com.helper.util.api.PropertiesConfig;
import com.helper.util.api.XmlDocumentParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import org.w3c.dom.Document;

/**
 * Provider for calling the IBKR Flex Web Service.
 */
public class IbkrApiProvider {
    private static final String DEFAULT_CONFIG_PATH = "config/ibkr-flex.properties";
    private static final String FLEX_BASE_URL = "https://ndcdyn.interactivebrokers.com/AccountManagement/FlexWebService";
    private static final String SEND_REQUEST_PATH = "/SendRequest";
    private static final String GET_STATEMENT_PATH = "/GetStatement";
    private static final DateTimeFormatter FLEX_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int MAX_DATE_RANGE_DAYS = 365;
    private static final String TOKEN_PROPERTY = "ibkr.flex.token";
    private static final String QUERY_ID_PROPERTY = "ibkr.flex.query-id";
    private static final String VERSION_PROPERTY = "ibkr.flex.version";

    private final HttpClient httpClient;
    private final XmlDocumentParser xmlDocumentParser;
    private final String token;
    private final String queryId;
    private final String version;

    /**
     * Creates a provider using the default local config file at
     * {@code config/ibkr-flex.properties}.
     */
    public IbkrApiProvider() {
        this(Path.of(DEFAULT_CONFIG_PATH));
    }

    /**
     * Creates a provider using a caller-supplied local config file.
     *
     * @param configPath path to a properties file containing the IBKR Flex token,
     *                   query ID, and API version
     */
    public IbkrApiProvider(Path configPath) {
        this(HttpClient.newHttpClient(), PropertiesConfig.load(configPath));
    }

    /**
     * Creates a provider with injected dependencies.
     *
     * <p>
     * This constructor is useful for tests or tools that want to provide an
     * in-memory config and a custom
     * {@link HttpClient}.
     *
     * @param httpClient HTTP client used to call the IBKR Flex Web Service
     * @param properties properties containing the IBKR Flex token, query ID, and
     *                   API version
     */
    public IbkrApiProvider(HttpClient httpClient, Properties properties) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
        this.xmlDocumentParser = new XmlDocumentParser();
        Objects.requireNonNull(properties, "properties");
        this.token = PropertiesConfig.requireProperty(properties, TOKEN_PROPERTY);
        this.queryId = PropertiesConfig.requireProperty(properties, QUERY_ID_PROPERTY);
        this.version = PropertiesConfig.requireProperty(properties, VERSION_PROPERTY);
    }

    public InputStream getFlexStatement(LocalDate fromDate, LocalDate toDate) throws IOException, InterruptedException {
        return new ByteArrayInputStream(getFlexStatementXml(fromDate, toDate).getBytes(StandardCharsets.UTF_8));
    }

    public String getFlexStatementXml(LocalDate fromDate, LocalDate toDate) throws IOException, InterruptedException {
        DateRangeValidator.requireMaxDays(fromDate, toDate, MAX_DATE_RANGE_DAYS);

        String statementReferenceCode = getRefCodeApi(fromDate, toDate);
        return getFlexStatementApi(statementReferenceCode);
    }

    private String getRefCodeApi(LocalDate fromDate, LocalDate toDate)
            throws IOException, InterruptedException {
        String responseXml = ApiRequest.sendGet(
                httpClient,
                createRefCodeUri(fromDate, toDate),
                "IBKR Flex request failed");
        requireSuccess(responseXml, "IBKR Flex SendRequest failed");
        return readRequiredXmlText(responseXml, "ReferenceCode", "IBKR Flex response did not include a reference code");
    }

    private URI createRefCodeUri(LocalDate fromDate, LocalDate toDate) {
        Map<String, String> queryParameters = new LinkedHashMap<>();
        queryParameters.put("t", token);
        queryParameters.put("q", queryId);
        queryParameters.put("v", version);
        queryParameters.put("fd", ApiRequest.formatDate(fromDate, FLEX_DATE_FORMATTER));
        queryParameters.put("td", ApiRequest.formatDate(toDate, FLEX_DATE_FORMATTER));

        return ApiRequest.createGetUri(FLEX_BASE_URL, SEND_REQUEST_PATH, queryParameters);
    }

    private String getFlexStatementApi(String statementReferenceCode)
            throws IOException, InterruptedException {
        String responseXml = ApiRequest.sendGet(
                httpClient,
                createFlexStatementUri(statementReferenceCode),
                "IBKR Flex request failed");
        requireSuccess(responseXml, "IBKR Flex GetStatement failed");
        return responseXml;
    }

    private URI createFlexStatementUri(String statementReferenceCode) {
        Map<String, String> queryParameters = new LinkedHashMap<>();
        queryParameters.put("t", token);
        queryParameters.put("q", statementReferenceCode);
        queryParameters.put("v", version);

        return ApiRequest.createGetUri(FLEX_BASE_URL, GET_STATEMENT_PATH, queryParameters);
    }

    private void requireSuccess(String xml, String failureMessage) {
        Document document = xmlDocumentParser.parse(xml);
        String status = xmlDocumentParser.findText(document, "Status").orElse(null);

        if ("Fail".equalsIgnoreCase(status)) {
            String errorCode = xmlDocumentParser.findText(document, "ErrorCode").orElse(null);
            String errorMessage = xmlDocumentParser.findText(document, "ErrorMessage").orElse(null);
            throw new IllegalStateException(failureMessage
                    + formatIbkrError(errorCode, errorMessage));
        }
    }

    private static String formatIbkrError(String errorCode, String errorMessage) {
        if (isBlank(errorCode) && isBlank(errorMessage)) {
            return "";
        }

        if (isBlank(errorCode)) {
            return ": " + errorMessage;
        }

        if (isBlank(errorMessage)) {
            return ": error code " + errorCode;
        }

        return ": error code " + errorCode + " - " + errorMessage;
    }

    private String readRequiredXmlText(String xml, String tagName, String errorMessage) {
        return xmlDocumentParser.requireText(xmlDocumentParser.parse(xml), tagName, errorMessage);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
