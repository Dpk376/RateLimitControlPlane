package com.example.gateway.core.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class TenantPluginId implements Serializable {
    private UUID tenantId;
    private String pluginId;

    public TenantPluginId() {}

    public TenantPluginId(UUID tenantId, String pluginId) {
        this.tenantId = tenantId;
        this.pluginId = pluginId;
    }

    public UUID getTenantId() { return tenantId; }
    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public String getPluginId() { return pluginId; }
    public void setPluginId(String pluginId) { this.pluginId = pluginId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TenantPluginId that = (TenantPluginId) o;
        return Objects.equals(tenantId, that.tenantId) && Objects.equals(pluginId, that.pluginId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, pluginId);
    }
}
