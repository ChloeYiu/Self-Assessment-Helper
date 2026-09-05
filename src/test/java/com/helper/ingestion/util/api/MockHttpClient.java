package com.helper.ingestion.util.api;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.PushPromiseHandler;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;

/**
 * Test HTTP client that returns queued string responses and records requests.
 */
public class MockHttpClient extends HttpClient {
    private final Queue<MockResponse> responses = new ArrayDeque<>();
    private final List<HttpRequest> requests = new ArrayList<>();
    private final SSLContext sslContext;

    public MockHttpClient() {
        this.sslContext = createSslContext();
    }

    public MockHttpClient respondWith(int statusCode, String body) {
        responses.add(new MockResponse(statusCode, body));
        return this;
    }

    public List<HttpRequest> getRequests() {
        return List.copyOf(requests);
    }

    public List<URI> getRequestedUris() {
        return requests.stream()
                .map(HttpRequest::uri)
                .toList();
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.empty();
    }

    @Override
    public Redirect followRedirects() {
        return Redirect.NEVER;
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.empty();
    }

    @Override
    public SSLContext sslContext() {
        return sslContext;
    }

    @Override
    public SSLParameters sslParameters() {
        return new SSLParameters();
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return Optional.empty();
    }

    @Override
    public Version version() {
        return Version.HTTP_1_1;
    }

    @Override
    public Optional<Executor> executor() {
        return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> HttpResponse<T> send(HttpRequest request, BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        requests.add(request);

        if (responses.isEmpty()) {
            throw new IllegalStateException("No mock HTTP response queued");
        }

        MockResponse response = responses.remove();
        return new MockHttpResponse<>(request, response.statusCode(), (T) response.body());
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
            HttpRequest request,
            BodyHandler<T> responseBodyHandler) {
        try {
            return CompletableFuture.completedFuture(send(request, responseBodyHandler));
        } catch (IOException exception) {
            return CompletableFuture.failedFuture(exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(exception);
        }
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
            HttpRequest request,
            BodyHandler<T> responseBodyHandler,
            PushPromiseHandler<T> pushPromiseHandler) {
        return sendAsync(request, responseBodyHandler);
    }

    private static SSLContext createSslContext() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, null, new SecureRandom());
            return context;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create mock SSL context", exception);
        }
    }

    private record MockResponse(int statusCode, String body) {
    }

    private record MockHttpResponse<T>(
            HttpRequest request,
            int statusCode,
            T body) implements HttpResponse<T> {

        @Override
        public Optional<HttpResponse<T>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return HttpHeaders.of(Map.of(), (name, value) -> true);
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return Optional.empty();
        }

        @Override
        public URI uri() {
            return request.uri();
        }

        @Override
        public Version version() {
            return Version.HTTP_1_1;
        }
    }
}
