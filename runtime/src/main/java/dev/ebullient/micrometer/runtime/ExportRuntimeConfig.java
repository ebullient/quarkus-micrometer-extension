package dev.ebullient.micrometer.runtime;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "micrometer.export", phase = ConfigPhase.RUN_TIME)
public class ExportRuntimeConfig {

    /**
     * Datadog registry configuration properties
     */
    @ConfigItem
    Map<String, String> datadog;

    /**
     * JMX registry configuration properties
     */
    @ConfigItem
    Map<String, String> jmx;

    /**
     * Prometheus registry configuration properties
     */
    @ConfigItem
    Map<String, String> prometheus;

    /**
     * Stackdriver registry configuration properties
     */
    @ConfigItem
    Map<String, String> stackdriver;
}
