package com.example.gateway.core.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    private UUID id;

    @Column(name = "api_key", nullable = false, unique = true)
    private String apiKey;

    @Column(name = "rate_limit_quota", nullable = false)
    private int rateLimitQuota;

    @Column(name = "rate_limit_replenish_rate", nullable = false)
    private int rateLimitReplenishRate;

    @Column(name = "downstream_url", nullable = false)
    private String downstreamUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Tenant() {}

    public Tenant(UUID id, String apiKey, int rateLimitQuota, int rateLimitReplenishRate, String downstreamUrl) {
        this.id = id;
        this.apiKey = apiKey;
        this.rateLimitQuota = rateLimitQuota;
        this.rateLimitReplenishRate = rateLimitReplenishRate;
        this.downstreamUrl = downstreamUrl;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getApiKey() { return apiKey; }
    public int getRateLimitQuota() { return rateLimitQuota; }
    public void setRateLimitQuota(int rateLimitQuota) { this.rateLimitQuota = rateLimitQuota; }
    public int getRateLimitReplenishRate() { return rateLimitReplenishRate; }
    public void setRateLimitReplenishRate(int rateLimitReplenishRate) { this.rateLimitReplenishRate = rateLimitReplenishRate; }
    public String getDownstreamUrl() { return downstreamUrl; }
    public void setDownstreamUrl(String downstreamUrl) { this.downstreamUrl = downstreamUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
