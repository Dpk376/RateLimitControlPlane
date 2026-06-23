package com.example.gateway.core.dto;

import java.util.UUID;

public record TenantResponse(UUID id, String apiKey, int rateLimitQuota, int rateLimitReplenishRate, String downstreamUrl) {}
