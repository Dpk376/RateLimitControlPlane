package com.example.gateway.core.filter;

import com.example.gateway.core.entity.Tenant;
import com.example.gateway.core.entity.TenantPlugin;
import com.example.gateway.core.ratelimit.RedisRateLimiterService;
import com.example.gateway.core.registry.PluginRegistry;
import com.example.gateway.core.repository.TenantPluginRepository;
import com.example.gateway.core.repository.TenantRepository;
import com.example.gateway.plugin.ApiGatewayPlugin;
import com.example.gateway.plugin.RequestContext;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
@Order(1)
public class GatewayFilter implements Filter {

    private final TenantRepository tenantRepository;
    private final TenantPluginRepository tenantPluginRepository;
    private final PluginRegistry pluginRegistry;
    private final RedisRateLimiterService rateLimiterService;
    private final MeterRegistry meterRegistry;

    public GatewayFilter(TenantRepository tenantRepository,
                         TenantPluginRepository tenantPluginRepository,
                         PluginRegistry pluginRegistry,
                         RedisRateLimiterService rateLimiterService,
                         MeterRegistry meterRegistry) {
        this.tenantRepository = tenantRepository;
        this.tenantPluginRepository = tenantPluginRepository;
        this.pluginRegistry = pluginRegistry;
        this.rateLimiterService = rateLimiterService;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip admin API and actuator from rate limiting/plugins
        String path = httpRequest.getRequestURI();
        if (path.startsWith("/api/admin") || path.startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        String apiKey = httpRequest.getHeader("X-API-KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Missing X-API-KEY header");
            return;
        }

        Optional<Tenant> tenantOpt = tenantRepository.findByApiKey(apiKey);
        if (tenantOpt.isEmpty()) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Invalid API Key");
            return;
        }
        Tenant tenant = tenantOpt.get();

        // Rate Limiting
        boolean allowed = rateLimiterService.isAllowed(
                tenant.getId().toString(),
                tenant.getRateLimitQuota(),
                tenant.getRateLimitReplenishRate()
        );

        if (!allowed) {
            meterRegistry.counter("gateway_rate_limited_total", "tenant", tenant.getId().toString()).increment();
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("Too Many Requests");
            return;
        }

        meterRegistry.counter("gateway_requests_total", "tenant", tenant.getId().toString(), "status", "200").increment();

        // Build Request Context
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = httpRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, httpRequest.getHeader(name));
        }
        RequestContext context = new RequestContext(httpRequest.getMethod(), path, headers);

        // Get enabled plugins for tenant
        List<TenantPlugin> enabledPlugins = tenantPluginRepository.findByTenantIdAndIsEnabledTrue(tenant.getId());
        List<ApiGatewayPlugin> activePlugins = new ArrayList<>();
        
        for (TenantPlugin tp : enabledPlugins) {
            pluginRegistry.getPlugin(tp.getPluginId()).ifPresent(activePlugins::add);
        }

        // Execute Pre-Hooks
        for (ApiGatewayPlugin plugin : activePlugins) {
            Timer.Sample sample = Timer.start(meterRegistry);
            boolean continueChain = plugin.executePre(context);
            sample.stop(meterRegistry.timer("gateway_plugin_execution_time_seconds", "plugin", plugin.getPluginId(), "phase", "pre"));
            
            if (!continueChain) {
                httpResponse.setStatus(context.getResponseStatus());
                return;
            }
        }

        try {
            // Execute Core Logic
            chain.doFilter(request, response);
        } finally {
            context.setResponseStatus(httpResponse.getStatus());
            
            // Execute Post-Hooks (in reverse order is usually a good practice, but we'll just do sequential here)
            for (ApiGatewayPlugin plugin : activePlugins) {
                Timer.Sample sample = Timer.start(meterRegistry);
                plugin.executePost(context);
                sample.stop(meterRegistry.timer("gateway_plugin_execution_time_seconds", "plugin", plugin.getPluginId(), "phase", "post"));
            }

            // Apply mutated headers to response (Note: this is simplified, in a real gateway we'd wrap the HttpServletResponse)
            for (Map.Entry<String, String> entry : context.getHeaders().entrySet()) {
                if (entry.getKey().startsWith("X-Gateway-")) {
                    httpResponse.setHeader(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
