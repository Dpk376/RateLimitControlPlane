package com.example.gateway.plugin;

import java.util.HashMap;
import java.util.Map;

public class RequestContext {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, Object> attributes;
    private int responseStatus = 200;

    public RequestContext(String method, String path, Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.headers = new HashMap<>(headers);
        this.attributes = new HashMap<>();
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }
}
