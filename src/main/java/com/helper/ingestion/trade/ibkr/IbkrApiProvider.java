package com.helper.ingestion.trade.ibkr;

import com.helper.issue.Issue;
import com.helper.issue.IssueCode;
import com.helper.issue.IssueSeverity;
import com.helper.util.api.ApiRequest;
import com.helper.util.api.DateRangeValidator;
import com.helper.util.api.PropertiesConfig;
import com.helper.util.api.XmlDocumentParser;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
    private static final long SEND_REQUEST_DELAY_MILLIS = 1_100L;
    private static final ZoneId IBKR_REPORTING_TIME_ZONE = ZoneId.of("America/New_York");
    private static final String TOKEN_PROPERTY = "ibkr.flex.token";
    private static final String QUERY_ID_PROPERTY = "ibkr.flex.query-id";
    private static final String POSITIONS_QUERY_ID_PROPERTY = "ibkr.flex.positions-query-id";
    private static final String VERSION_PROPERTY = "ibkr.flex.version";

    private final HttpClient httpClient;
    private final XmlDocumentParser xmlDocumentParser;
    private final String token;
    private final String activityQueryId;
    private final String positionsQueryId;
    private final String version;
    private boolean hasSentFlexRequest;

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
        this.activityQueryId = PropertiesConfig.requireProperty(properties, QUERY_ID_PROPERTY);
        this.positionsQueryId = PropertiesConfig.requireProperty(properties, POSITIONS_QUERY_ID_PROPERTY);
        this.version = PropertiesConfig.requireProperty(properties, VERSION_PROPERTY);
    }

    /**
     * Retrieves every Activity Flex statement needed for the requested inclusive date range.
     *
     * <p>IBKR limits one Flex request to 365 days. This method hides that transport limit
     * by issuing adjacent requests and returning their statements in chronological order.</p>
     */
    /** Retrieves Activity Flex statement XML for an inclusive date range. */
    public IbkrApiResult getActivityStatementsXml(
            LocalDate fromDate,
            LocalDate toDate
    ) throws IOException, InterruptedException {
        return getFlexStatementsXml(activityQueryId, fromDate, toDate);
    }

    /** Retrieves Open Positions Flex statement XML for one reporting date. */
    public IbkrApiResult getPositionStatementXml(LocalDate positionDate)
            throws IOException, InterruptedException {
        return getFlexStatementsXml(positionsQueryId, positionDate, positionDate);
    }

    private IbkrApiResult getFlexStatementsXml(
            String selectedQueryId,
            LocalDate fromDate,
            LocalDate toDate
    ) throws IOException, InterruptedException {
        String requestQueryId = requireQueryId(selectedQueryId);
        LocalDate effectiveToDate = truncateRequestedEndDate(fromDate, toDate);
        List<String> statements = new ArrayList<>();
        List<Issue> issues = new ArrayList<>();
        LocalDate requestStart = fromDate;

        while (!requestStart.isAfter(effectiveToDate)) {
            LocalDate requestEnd = requestStart.plusDays(MAX_DATE_RANGE_DAYS - 1);
            if (requestEnd.isAfter(effectiveToDate)) {
                requestEnd = effectiveToDate;
            }
            LocalDate chunkStart = requestStart;
            LocalDate chunkEnd = requestEnd;
            try {
                statements.add(downloadFlexStatementForSplitRange(
                        requestQueryId,
                        chunkStart,
                        chunkEnd));
            } catch (IbkrFlexApiException exception) {
                issues.add(handleIbkrFlexApiException(exception, chunkStart, chunkEnd));
            }
            requestStart = requestEnd.plusDays(1);
        }

        return new IbkrApiResult(statements, issues, effectiveToDate);
    }

    private String downloadFlexStatementForSplitRange(
            String selectedQueryId,
            LocalDate fromDate,
            LocalDate toDate
    ) throws IOException, InterruptedException {
        if (hasSentFlexRequest) {
            waitBeforeNextSendRequest();
        }
        hasSentFlexRequest = true;
        return getFlexStatementXmlForRequest(selectedQueryId, fromDate, toDate);
    }

    private static void waitBeforeNextSendRequest() throws InterruptedException {
        Thread.sleep(SEND_REQUEST_DELAY_MILLIS);
    }

    /** Successful statement XML, non-fatal unavailable ranges, and actual end-date coverage. */
    public record IbkrApiResult(
            List<String> statementXmls,
            List<Issue> issues,
            LocalDate effectiveEndDate
    ) {
        public IbkrApiResult {
            statementXmls = List.copyOf(Objects.requireNonNull(statementXmls, "statementXmls"));
            issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
            effectiveEndDate = Objects.requireNonNull(effectiveEndDate, "effectiveEndDate");
        }
    }

    /**
     * Truncates an end date to the latest completed IBKR reporting business day.
     *
     * <p>Activity Flex Query data is available only through the previous business day.
     * IBKR reporting uses the New York calendar, including daylight-saving changes.</p>
     *
     * @throws IllegalArgumentException if the requested range begins after the latest available date
     */
    public LocalDate truncateRequestedEndDate(LocalDate fromDate, LocalDate toDate) {
        DateRangeValidator.requireValidRange(fromDate, toDate);
        LocalDate latestAvailableDate = getLatestAvailableStatementDate();
        if (fromDate.isAfter(latestAvailableDate)) {
            throw new IllegalArgumentException(
                    "IBKR Flex reports are available only through " + latestAvailableDate);
        }
        return toDate.isAfter(latestAvailableDate) ? latestAvailableDate : toDate;
    }

    /** Returns the previous weekday in IBKR's New York reporting time zone. */
    public LocalDate getLatestAvailableStatementDate() {
        LocalDate latestAvailableDate = LocalDate.now(IBKR_REPORTING_TIME_ZONE).minusDays(1);
        while (latestAvailableDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || latestAvailableDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
            latestAvailableDate = latestAvailableDate.minusDays(1);
        }
        return latestAvailableDate;
    }

    private String getFlexStatementXmlForRequest(String selectedQueryId, LocalDate fromDate, LocalDate toDate)
            throws IOException, InterruptedException {
        DateRangeValidator.requireMaxDays(fromDate, toDate, MAX_DATE_RANGE_DAYS);
        String statementReferenceCode = getRefCodeApi(selectedQueryId, fromDate, toDate);
        return getFlexStatementApi(statementReferenceCode);
    }

    private String getRefCodeApi(String selectedQueryId, LocalDate fromDate, LocalDate toDate)
            throws IOException, InterruptedException {
        String responseXml = ApiRequest.sendGet(
                httpClient,
                createRefCodeUri(selectedQueryId, fromDate, toDate),
                "IBKR Flex request failed");
        requireSuccess(responseXml, "IBKR Flex SendRequest failed");
        Document response = xmlDocumentParser.parse(responseXml);
        return xmlDocumentParser.findText(response, "ReferenceCode")
                .filter(referenceCode -> !referenceCode.isBlank())
                .orElseThrow(() -> missingReferenceCode(response));
    }

    private URI createRefCodeUri(String selectedQueryId, LocalDate fromDate, LocalDate toDate) {
        Map<String, String> queryParameters = new LinkedHashMap<>();
        queryParameters.put("t", token);
        queryParameters.put("q", selectedQueryId);
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
            throw new IbkrFlexApiException(
                    failureMessage + formatIbkrError(errorCode, errorMessage),
                    errorCode,
                    errorMessage);
        }
    }

    private static Issue handleIbkrFlexApiException(
            IbkrFlexApiException exception,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        String errorCode = exception.errorCode();
        if (!"1003".equals(errorCode)) {
            throw exception;
        }
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("fromDate", fromDate.toString());
        attributes.put("toDate", toDate.toString());
        if (!isBlank(errorCode)) {
            attributes.put("providerErrorCode", errorCode);
        }
        if (!isBlank(exception.errorMessage())) {
            attributes.put("providerErrorMessage", exception.errorMessage());
        }
        return new Issue(
                IssueCode.IBKR_STATEMENT_UNAVAILABLE,
                IssueSeverity.INFO,
                "ibkr-flex",
                attributes);
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

    private IllegalStateException missingReferenceCode(Document response) {
        String status = xmlDocumentParser.findText(response, "Status").orElse("missing");
        String errorCode = xmlDocumentParser.findText(response, "ErrorCode").orElse(null);
        String errorMessage = xmlDocumentParser.findText(response, "ErrorMessage").orElse(null);
        return new IllegalStateException("IBKR Flex response did not include a reference code"
                + " (status=" + status + ")"
                + formatIbkrError(errorCode, errorMessage));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String requireQueryId(String selectedQueryId) {
        if (isBlank(selectedQueryId)) {
            throw new IllegalArgumentException("selectedQueryId must not be blank");
        }
        return selectedQueryId.trim();
    }

    private static final class IbkrFlexApiException extends IllegalStateException {
        private final String errorCode;
        private final String errorMessage;

        private IbkrFlexApiException(String message, String errorCode, String errorMessage) {
            super(message);
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        private String errorCode() {
            return errorCode;
        }

        private String errorMessage() {
            return errorMessage;
        }
    }

}
