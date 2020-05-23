package dev.ebullient.micrometer.runtime.config;

import java.util.Optional;

import dev.ebullient.micrometer.runtime.config.MicrometerConfig.CapabilityEnabled;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class JmxConfig implements CapabilityEnabled {
    /**
     * Support for export to JMX
     * <p>
     * Support for JMX will be enabled if micrometer
     * support is enabled, the JmxMeterRegistry is on the classpath
     * and either this value is true, or this value is unset and
     * {@code quarkus.micrometer.registry-enabled-default} is true.
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
