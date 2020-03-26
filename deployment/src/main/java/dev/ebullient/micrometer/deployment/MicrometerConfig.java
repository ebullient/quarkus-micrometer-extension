package dev.ebullient.micrometer.deployment;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "micrometer")
final class MicrometerConfig {

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
