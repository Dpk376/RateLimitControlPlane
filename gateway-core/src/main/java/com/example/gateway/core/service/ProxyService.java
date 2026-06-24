package com.example.gateway.core.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class ProxyService {

    private final HttpClient httpClient;

    public ProxyService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @CircuitBreaker(name = "downstream")
    @Retry(name = "downstream")
    public HttpResponse<InputStream> forwardRequest(HttpRequest httpRequest) throws Exception {
        HttpResponse<InputStream> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() >= 500) {
            try (InputStream body = response.body()) {
                // close the body
            }
            throw new RuntimeException("Downstream server returned " + response.statusCode());
        }
        return response;
    }
}
