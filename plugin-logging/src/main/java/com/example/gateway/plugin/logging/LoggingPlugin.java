package com.example.gateway.plugin.logging;

import com.example.gateway.plugin.ApiGatewayPlugin;
import com.example.gateway.plugin.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingPlugin implements ApiGatewayPlugin {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingPlugin.class);

    @Override
    public String getPluginId() {
        return "plugin-logging";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean executePre(RequestContext context) {
        log.info("Pre-Request: {} {}", context.getMethod(), context.getPath());
        context.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void executePost(RequestContext context) {
        Long startTime = (Long) context.getAttribute("startTime");
        long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;
        log.info("Post-Response: {} {} - Status: {} ({}ms)", 
                 context.getMethod(), context.getPath(), context.getResponseStatus(), duration);
    }
}
