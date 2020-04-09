package dev.ebullient.micrometer.deployment;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "micrometer.export.stackdriver", phase = ConfigPhase.BUILD_TIME)
final class StackdriverBuildTimeConfig {
    static final Class<?> REGISTRY_CLASS = io.micrometer.stackdriver.StackdriverMeterRegistry.class;
    static final String REGISTRY_CLASS_NAME = "io.micrometer.stackdriver.StackdriverMeterRegistry";

    static class StackdriverEnabled implements BooleanSupplier {
        MicrometerBuildTimeConfig mConfig;
        StackdriverBuildTimeConfig sConfig;

        public boolean getAsBoolean() {
            boolean enabled = false;
            // TODO: Can't yet check for classes on the classpath in supplier
            //if (MicrometerProcessor.isInClasspath(REGISTRY_CLASS_NAME)) {
            enabled = mConfig.checkEnabledWithDefault(sConfig.enabled);
            //}
            return enabled;
        }
    }

    /**
     * If the stackdriver metrics registry is enabled.
     */
    @ConfigItem
    Optional<Boolean> enabled;

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "{enabled=" + enabled
                + '}';
    }
}
