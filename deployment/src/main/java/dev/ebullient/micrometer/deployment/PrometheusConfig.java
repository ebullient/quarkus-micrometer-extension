package dev.ebullient.micrometer.deployment;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "micrometer.export.prometheus")
final class PrometheusConfig {
    /**
     * Default path for the prometheus endpoint
     */
    @ConfigItem(defaultValue = "/prometheus")
    String path;

    /**
     * If the prometheus endpoint is enabled.
     */
    @ConfigItem(defaultValue = "true")
    public boolean enabled;

    @Override
    public String toString() {
        return "PrometheusConfig{"
                + ", path='" + path
                + ", enabled=" + enabled
                + '}';
    }
}
