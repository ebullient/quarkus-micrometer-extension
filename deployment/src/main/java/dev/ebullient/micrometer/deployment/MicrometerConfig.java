package dev.ebullient.micrometer.deployment;

import java.util.function.BooleanSupplier;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "micrometer")
final class MicrometerConfig {

    static class MicrometerEnabled implements BooleanSupplier {
        MicrometerConfig mConfig;

        public boolean getAsBoolean() {
            return mConfig.enabled;
        }
    }

    /**
     * If the micrometer extension is enabled.
     */
    @ConfigItem(defaultValue = "true")
    public boolean enabled;

    @Override
    public String toString() {
        return "MicrometerConfig{"
                + ", enabled=" + enabled
                + '}';
    }
}
