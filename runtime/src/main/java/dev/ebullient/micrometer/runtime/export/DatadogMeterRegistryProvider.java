package dev.ebullient.micrometer.runtime.export;

import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.Config;
import org.jboss.logging.Logger;

import dev.ebullient.micrometer.runtime.MeterFilterConstraint;
import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.datadog.DatadogConfig;
import io.micrometer.datadog.DatadogMeterRegistry;
import io.quarkus.arc.DefaultBean;

@Singleton
public class DatadogMeterRegistryProvider {
    private static final Logger log = Logger.getLogger(DatadogMeterRegistryProvider.class);
    static final String PREFIX = "quarkus.micrometer.export.datadog.";
    static final String PUBLISH = "datadog.publish";
    static final String ENABLED = "datadog.enabled";

    final Instance<MeterFilter> filters;

    DatadogMeterRegistryProvider(
            @MeterFilterConstraint(type = DatadogMeterRegistry.class) Instance<MeterFilter> filters) {
        log.debugf("DatadogMeterRegistryProvider initialized. hasFilters=%s", !filters.isUnsatisfied());
        this.filters = filters;
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
        DatadogMeterRegistry registry = DatadogMeterRegistry.builder(config)
                .clock(clock)
                .build();
        // Apply datadog-specific meter filters
        if (!filters.isUnsatisfied()) {
            filters.stream().forEach(registry.config()::meterFilter);
        }
        return registry;
    }
}
