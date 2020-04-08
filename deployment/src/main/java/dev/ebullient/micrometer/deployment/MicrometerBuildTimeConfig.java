package dev.ebullient.micrometer.deployment;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "micrometer", phase = ConfigPhase.BUILD_TIME)
final class MicrometerBuildTimeConfig {

    static class MicrometerEnabled implements BooleanSupplier {
        MicrometerBuildTimeConfig mConfig;

        public boolean getAsBoolean() {
            return mConfig.enabled;
        }
    }

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
     * For Meter Registry configurations with optional 'enabled' attributes,
     * determine whether or not the registry is enabled using {@link #registryEnabledDefault}
     * as the default value.
     */
    public boolean checkEnabledWithDefault(Optional<Boolean> configValue) {
        if (enabled) {
            if (configValue.isPresent()) {
                return configValue.get();
            } else {
                return registryEnabledDefault;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "{enabled=" + enabled
                + ",registryEnabledDefault=" + registryEnabledDefault
                + '}';
    }
}
