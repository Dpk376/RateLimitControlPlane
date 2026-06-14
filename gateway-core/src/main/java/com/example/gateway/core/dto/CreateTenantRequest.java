package com.example.gateway.core.dto;

public record CreateTenantRequest(String apiKey, int rateLimitQuota, int rateLimitReplenishRate) {}
