package com.example.gateway.core.repository;

import com.example.gateway.core.entity.TenantPlugin;
import com.example.gateway.core.entity.TenantPluginId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TenantPluginRepository extends JpaRepository<TenantPlugin, TenantPluginId> {
    List<TenantPlugin> findByTenantIdAndIsEnabledTrue(UUID tenantId);
}
