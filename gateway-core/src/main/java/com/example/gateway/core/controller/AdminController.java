package com.example.gateway.core.controller;

import com.example.gateway.core.dto.CreateTenantRequest;
import com.example.gateway.core.dto.TenantResponse;
import com.example.gateway.core.dto.UpdateQuotaRequest;
import com.example.gateway.core.dto.TogglePluginRequest;
import com.example.gateway.core.service.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/tenants")
public class AdminController {

    private final TenantService tenantService;

    public AdminController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantResponse createTenant(@RequestBody CreateTenantRequest request) {
        return tenantService.createTenant(request);
    }

    @PutMapping("/{id}/rate-limits")
    public TenantResponse updateRateLimits(@PathVariable UUID id, @RequestBody UpdateQuotaRequest request) {
        return tenantService.updateQuota(id, request);
    }

    @PostMapping("/{id}/plugins/{pluginId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void togglePlugin(@PathVariable UUID id, @PathVariable String pluginId, @RequestBody TogglePluginRequest request) {
        tenantService.togglePlugin(id, pluginId, request);
    }
}
