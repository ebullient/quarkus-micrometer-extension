package dev.ebullient.micrometer.deployment;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "micrometer.export.prometheus", phase = ConfigPhase.BUILD_TIME)
final class PrometheusBuildTimeConfig {
    static final String REGISTRY_CLASS = "io.micrometer.prometheus.PrometheusMeterRegistry";

    static class PrometheusEnabled implements BooleanSupplier {
        MicrometerBuildTimeConfig mConfig;
        PrometheusBuildTimeConfig pConfig;

        public boolean getAsBoolean() {
            boolean enabled = false;
            if (MicrometerProcessor.isInClasspath(REGISTRY_CLASS)) {
                enabled = mConfig.checkEnabledWithDefault(pConfig.enabled);
            }
            return enabled;
        }
    }

    /**
     * Default path for the prometheus endpoint
     */
    @ConfigItem(defaultValue = "/prometheus")
    String path;

    /**
     * If the Prometheus micrometer registry is enabled.
     */
    @ConfigItem
    Optional<Boolean> enabled;

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "{path='" + path
                + ",enabled=" + enabled
                + '}';
    }
}
