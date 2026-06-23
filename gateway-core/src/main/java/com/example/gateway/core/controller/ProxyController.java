package com.example.gateway.core.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

@RestController
public class ProxyController {

    private final HttpClient httpClient;

    public ProxyController() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @RequestMapping("/**")
    public void proxy(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String path = request.getRequestURI();
        
        // Skip proxying admin and actuator
        if (path.startsWith("/api/admin") || path.startsWith("/actuator")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String downstreamUrlBase = (String) request.getAttribute("downstream_url");
        if (downstreamUrlBase == null) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "No downstream URL configured for tenant");
            return;
        }

        String queryString = request.getQueryString();
        String targetUrl = downstreamUrlBase + path + (queryString != null ? "?" + queryString : "");

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .method(request.getMethod(),
                        request.getContentLength() > 0 ?
                                HttpRequest.BodyPublishers.ofInputStream(() -> {
                                    try {
                                        return request.getInputStream();
                                    } catch (Exception e) {
                                        throw new RuntimeException(e);
                                    }
                                }) : HttpRequest.BodyPublishers.noBody());

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (isRestrictedHeader(headerName)) {
                continue;
            }
            requestBuilder.header(headerName, request.getHeader(headerName));
        }

        HttpRequest httpRequest = requestBuilder.build();

        HttpResponse<InputStream> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());

        response.setStatus(httpResponse.statusCode());
        for (Map.Entry<String, List<String>> header : httpResponse.headers().map().entrySet()) {
            if (isRestrictedHeader(header.getKey())) {
                continue;
            }
            for (String val : header.getValue()) {
                response.addHeader(header.getKey(), val);
            }
        }

        try (InputStream body = httpResponse.body()) {
            body.transferTo(response.getOutputStream());
        }
    }

    private boolean isRestrictedHeader(String headerName) {
        String lower = headerName.toLowerCase();
        return lower.equals("host") || lower.equals("connection") || lower.equals("content-length") || lower.equals("transfer-encoding");
    }
}
