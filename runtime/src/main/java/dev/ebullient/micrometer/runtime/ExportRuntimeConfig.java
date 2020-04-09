package dev.ebullient.micrometer.runtime;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "micrometer.export", phase = ConfigPhase.RUN_TIME)
public class ExportRuntimeConfig {

    /**
     * Stackdriver registry configuration properties
     */
    @ConfigItem
    Map<String, String> stackdriver;

    /**
     * Prometheus registry configuration properties
     */
    @ConfigItem
    Map<String, String> prometheus;

}
