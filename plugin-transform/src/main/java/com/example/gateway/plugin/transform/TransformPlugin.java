package com.example.gateway.plugin.transform;

import com.example.gateway.plugin.ApiGatewayPlugin;
import com.example.gateway.plugin.RequestContext;

public class TransformPlugin implements ApiGatewayPlugin {

    @Override
    public String getPluginId() {
        return "plugin-transform";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean executePre(RequestContext context) {
        // Example: Add a correlation ID if missing
        if (!context.getHeaders().containsKey("X-Correlation-ID")) {
            context.addHeader("X-Correlation-ID", "generated-" + System.currentTimeMillis());
        }
        return true;
    }

    @Override
    public void executePost(RequestContext context) {
        // Example: Inject a standard header into the response
        context.addHeader("X-Gateway-Processed-By", "TransformPlugin v1.0.0");
    }
}
