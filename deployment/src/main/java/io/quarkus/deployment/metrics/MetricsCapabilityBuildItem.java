package io.quarkus.deployment.metrics;

import io.quarkus.builder.item.SimpleBuildItem;

public final class MetricsCapabilityBuildItem extends SimpleBuildItem {
    @FunctionalInterface
    public interface MetricsCapability<String> {
        boolean isSupported(String value);
    }

    final String path;
    final MetricsCapability<String> metricsCapability;

    public MetricsCapabilityBuildItem(MetricsCapability<String> metricsCapability) {
        this(metricsCapability, null);
    }

    public MetricsCapabilityBuildItem(MetricsCapability<String> metricsCapability, String path) {
        this.metricsCapability = metricsCapability;
        this.path = path;
    }

    public boolean metricsSupported(String name) {
        return metricsCapability.isSupported(name);
    }

    public String metricsEndpoint() {
        return path;
    }
}
