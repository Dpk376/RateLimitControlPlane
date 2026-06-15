package com.example.gateway.core.dto;

public record CreateTenantRequest(int rateLimitQuota, int rateLimitReplenishRate) {}
