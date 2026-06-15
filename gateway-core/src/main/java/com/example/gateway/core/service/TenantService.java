package com.example.gateway.core.service;

import com.example.gateway.core.dto.CreateTenantRequest;
import com.example.gateway.core.dto.TenantResponse;
import com.example.gateway.core.dto.UpdateQuotaRequest;
import com.example.gateway.core.dto.TogglePluginRequest;
import com.example.gateway.core.entity.Tenant;
import com.example.gateway.core.entity.TenantPlugin;
import com.example.gateway.core.entity.TenantPluginId;
import com.example.gateway.core.repository.TenantRepository;
import com.example.gateway.core.repository.TenantPluginRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantPluginRepository tenantPluginRepository;

    public TenantService(TenantRepository tenantRepository, TenantPluginRepository tenantPluginRepository) {
        this.tenantRepository = tenantRepository;
        this.tenantPluginRepository = tenantPluginRepository;
    }

    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        String generatedApiKey = generateApiKey();
        Tenant tenant = new Tenant(UUID.randomUUID(), generatedApiKey, request.rateLimitQuota(), request.rateLimitReplenishRate());
        tenantRepository.save(tenant);
        return mapToResponse(tenant);
    }

    private String generateApiKey() {
        byte[] randomBytes = new byte[32];
        new java.security.SecureRandom().nextBytes(randomBytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @Transactional
    public TenantResponse updateQuota(UUID tenantId, UpdateQuotaRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        tenant.setRateLimitQuota(request.rateLimitQuota());
        tenant.setRateLimitReplenishRate(request.rateLimitReplenishRate());
        tenantRepository.save(tenant);
        return mapToResponse(tenant);
    }

    @Transactional
    public void togglePlugin(UUID tenantId, String pluginId, TogglePluginRequest request) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        
        TenantPluginId id = new TenantPluginId(tenantId, pluginId);
        TenantPlugin tenantPlugin = tenantPluginRepository.findById(id)
                .orElse(new TenantPlugin(tenantId, pluginId, request.enabled(), request.configJson()));
        
        tenantPlugin.setEnabled(request.enabled());
        tenantPlugin.setConfigJson(request.configJson());
        tenantPluginRepository.save(tenantPlugin);
    }

    private TenantResponse mapToResponse(Tenant tenant) {
        return new TenantResponse(tenant.getId(), tenant.getApiKey(), tenant.getRateLimitQuota(), tenant.getRateLimitReplenishRate());
    }
}
