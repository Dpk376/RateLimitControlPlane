package com.example.gateway.core.entity;

import jakarta.persistence.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenant_plugins")
@IdClass(TenantPluginId.class)
public class TenantPlugin {

    @Id
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Id
    @Column(name = "plugin_id", nullable = false)
    private String pluginId;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled;

    @Column(name = "config_json", columnDefinition = "jsonb")
    private String configJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public TenantPlugin() {}

    public TenantPlugin(UUID tenantId, String pluginId, boolean isEnabled, String configJson) {
        this.tenantId = tenantId;
        this.pluginId = pluginId;
        this.isEnabled = isEnabled;
        this.configJson = configJson;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getTenantId() { return tenantId; }
    public String getPluginId() { return pluginId; }
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
    public String getConfigJson() { return configJson; }
    public void setConfigJson(String configJson) { this.configJson = configJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
