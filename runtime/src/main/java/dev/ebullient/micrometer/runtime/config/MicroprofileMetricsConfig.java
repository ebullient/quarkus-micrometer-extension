package dev.ebullient.micrometer.runtime.config;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

/**
 * Build / static runtime config for the Microprofile Metrics Binder
 */
@ConfigGroup
public class MicroprofileMetricsConfig implements MicrometerConfig.CapabilityEnabled {
    /**
     * Microprofile Metrics support.
     * <p>
     * Support for Microprofile metrics will be enabled if micrometer
     * support is enabled, Microprofile APIs are on the classpath
     * and either this value is true, or this value is unset and
     * {@code quarkus.micrometer.binder-enabled-default} is true.
     */
    @ConfigItem
    public Optional<Boolean> enabled;

    @Override
    public Optional<Boolean> getEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "{enabled=" + enabled
                + '}';
    }
}
