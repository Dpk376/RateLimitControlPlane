package com.example.gateway.plugin;

public interface ApiGatewayPlugin {
    
    /**
     * Unique identifier for the plugin.
     */
    String getPluginId();

    /**
     * Version of the plugin.
     */
    String getVersion();

    /**
     * Hook to execute before the core service logic.
     * @param context the current request context
     * @return true to continue the filter chain, false to abort
     */
    boolean executePre(RequestContext context);

    /**
     * Hook to execute after the core service logic.
     * @param context the current request context
     */
    void executePost(RequestContext context);
}
