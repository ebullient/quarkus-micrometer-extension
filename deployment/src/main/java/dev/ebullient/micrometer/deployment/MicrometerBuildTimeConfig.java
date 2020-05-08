package dev.ebullient.micrometer.deployment;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

/**
 * Global configuration for the Micrometer extension
 */
@ConfigRoot(name = "micrometer", phase = ConfigPhase.BUILD_TIME)
public final class MicrometerBuildTimeConfig {

    /**
     * If the micrometer extension is enabled.
     */
    @ConfigItem(defaultValue = "true")
    public boolean enabled;

    /**
     * Global default value for discovered MeterRegistries
     */
    @ConfigItem(defaultValue = "true")
    public boolean registryEnabledDefault;

    /**
     * Global default value for discovered MeterBinders
     */
    @ConfigItem(defaultValue = "true")
    public boolean binderEnabledDefault;

    /**
     * For Meter Registry configurations with optional 'enabled' attributes,
     * determine whether or not the registry is enabled using {@link #registryEnabledDefault}
     * as the default value.
     */
    public boolean checkRegistryEnabledWithDefault(Optional<Boolean> configValue) {
        if (enabled) {
            if (configValue.isPresent()) {
                return configValue.get();
            } else {
                return registryEnabledDefault;
            }
        }
        return false;
    }

    /**
     * For Meter Binder configurations with optional 'enabled' attributes,
     * determine whether or not the binder is enabled using {@link #binderEnabledDefault}
     * as the default value.
     */
    public boolean checkBinderEnabledWithDefault(Optional<Boolean> configValue) {
        if (enabled) {
            if (configValue.isPresent()) {
                return configValue.get();
            } else {
                return binderEnabledDefault;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "{enabled=" + enabled
                + ",binderEnabledDefault=" + binderEnabledDefault
                + ",registryEnabledDefault=" + registryEnabledDefault
                + '}';
    }
}
