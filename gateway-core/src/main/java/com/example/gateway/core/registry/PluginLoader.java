package com.example.gateway.core.registry;

import com.example.gateway.plugin.ApiGatewayPlugin;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

@Service
public class PluginLoader {
    private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);

    private final PluginRegistry registry;
    private final String pluginDir;
    private FileAlterationMonitor monitor;

    public PluginLoader(PluginRegistry registry, @Value("${gateway.plugin-dir:./plugins}") String pluginDir) {
        this.registry = registry;
        this.pluginDir = pluginDir;
    }

    @PostConstruct
    public void init() throws Exception {
        File dir = new File(pluginDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Load existing plugins
        loadPluginsFromDirectory(dir);

        // Watch for changes
        FileAlterationObserver observer = new FileAlterationObserver(dir);
        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                if (file.getName().endsWith(".jar")) {
                    log.info("New plugin JAR detected: {}", file.getName());
                    loadPlugin(file);
                }
            }

            @Override
            public void onFileChange(File file) {
                if (file.getName().endsWith(".jar")) {
                    log.info("Plugin JAR updated: {}", file.getName());
                    loadPlugin(file);
                }
            }
            
            @Override
            public void onFileDelete(File file) {
                log.info("Plugin JAR deleted: {}", file.getName());
                // In a full implementation, we'd map files to plugin IDs and unregister.
            }
        });

        monitor = new FileAlterationMonitor(5000, observer); // check every 5s
        monitor.start();
        log.info("Started watching {} for plugin changes", dir.getAbsolutePath());
    }

    @PreDestroy
    public void cleanup() throws Exception {
        if (monitor != null) {
            monitor.stop();
        }
    }

    private void loadPluginsFromDirectory(File dir) {
        File[] files = dir.listFiles((d, name) -> name.endsWith(".jar"));
        if (files != null) {
            for (File file : files) {
                loadPlugin(file);
            }
        }
    }

    private void loadPlugin(File file) {
        try {
            URL url = file.toURI().toURL();
            URLClassLoader classLoader = new URLClassLoader(new URL[]{url}, this.getClass().getClassLoader());
            ServiceLoader<ApiGatewayPlugin> serviceLoader = ServiceLoader.load(ApiGatewayPlugin.class, classLoader);
            
            for (ApiGatewayPlugin plugin : serviceLoader) {
                log.info("Loaded plugin: {} (v{})", plugin.getPluginId(), plugin.getVersion());
                registry.register(plugin);
            }
        } catch (Exception e) {
            log.error("Failed to load plugin from {}", file.getName(), e);
        }
    }
}
