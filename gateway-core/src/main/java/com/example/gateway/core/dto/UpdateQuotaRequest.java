package com.example.gateway.core.dto;

public record UpdateQuotaRequest(int rateLimitQuota, int rateLimitReplenishRate) {}
