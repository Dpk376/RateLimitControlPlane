package com.example.gateway.core.registry;

import com.example.gateway.plugin.ApiGatewayPlugin;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PluginRegistry {

    private final Map<String, ApiGatewayPlugin> plugins = new ConcurrentHashMap<>();

    public void register(ApiGatewayPlugin plugin) {
        plugins.put(plugin.getPluginId(), plugin);
    }

    public void unregister(String pluginId) {
        plugins.remove(pluginId);
    }

    public Optional<ApiGatewayPlugin> getPlugin(String pluginId) {
        return Optional.ofNullable(plugins.get(pluginId));
    }

    public Collection<ApiGatewayPlugin> getAllPlugins() {
        return plugins.values();
    }
}
