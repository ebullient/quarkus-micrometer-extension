package dev.ebullient.micrometer.runtime;

import java.util.Map;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.Config;
import org.jboss.logging.Logger;

import io.micrometer.core.instrument.Clock;
import io.micrometer.datadog.DatadogConfig;
import io.micrometer.datadog.DatadogMeterRegistry;
import io.quarkus.arc.DefaultBean;

@Singleton
public class DatadogMeterRegistryProvider {
    private static final Logger log = Logger.getLogger(DatadogMeterRegistryProvider.class);
    static final String PREFIX = "quarkus.micrometer.export.datadog.";
    static final String PUBLISH = "datadog.publish";
    static final String ENABLED = "datadog.enabled";

    DatadogMeterRegistryProvider() {
        log.debug("DatadogMeterRegistryProvider initialized");
    }

    @Produces
    @Singleton
    @DefaultBean
    public DatadogConfig configure(Config config) {
        final Map<String, String> properties = MicrometerRecorder.captureProperties(config, PREFIX);

        // Special check: if publish is set, override the value of enabled
        // Specifically, The datadov registry must be enabled for this
        // Provider to even be present. If this instance (at runtime) wants
        // to prevent metrics from being published, then it would set
        // quarkus.micrometer.export.datadog.publish=false
        if (properties.containsKey(PUBLISH)) {
            properties.put(ENABLED, properties.get(PUBLISH));
        }

        return new DatadogConfig() {
            @Override
            public String get(String key) {
                return properties.get(key);
            }
        };
    }

    @Produces
    @Singleton
    @DefaultBean
    public DatadogMeterRegistry registry(DatadogConfig config, Clock clock) {
        return DatadogMeterRegistry.builder(config)
                .clock(clock)
                .build();
    }
}
