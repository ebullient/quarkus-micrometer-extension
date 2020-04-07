package dev.ebullient.micrometer.runtime;

import static io.quarkus.runtime.annotations.ConfigPhase.BUILD_AND_RUN_TIME_FIXED;

import java.util.Optional;
import java.util.function.BooleanSupplier;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "micrometer.export.stackdriver", phase = BUILD_AND_RUN_TIME_FIXED)
public class StackdriverConfig {

    public static class StackdriverEnabled implements BooleanSupplier {
        StackdriverConfig sConfig;

        public boolean getAsBoolean() {
            return sConfig.enabled;
        }
    }

    /**
     * If the stackdriver metrics are enabled.
     *
     * You will probably want to have this disabled on local environment.
     */
    @ConfigItem(defaultValue = "false")
    public boolean enabled;

    /**
     * Project id where metrics will be pushed.
     */
    @ConfigItem
    public Optional<String> projectId;

    /**
     * The interval at which metrics are sent to Stackdriver Monitoring. The default is 1 minute.
     *
     * Must be in ISO-8601 duration format PnDTnHnMn.nS e.g. PT30s, PT1m etc.
     */
    @ConfigItem(defaultValue = "PT1m")
    public String step;

    /**
     * Stackdriver resource type. Default is global.
     */
    @ConfigItem(defaultValue = "global")
    public String resourceType;

    @Override
    public String toString() {
        return "StackdriverConfig{"
                + ", enabled=" + enabled
                + ", project=" + projectId
                + ", step=" + step
                + ", resourceType=" + resourceType
                + '}';
    }

}
