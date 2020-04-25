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
import io.micrometer.stackdriver.StackdriverConfig;
import io.micrometer.stackdriver.StackdriverMeterRegistry;
import io.quarkus.arc.DefaultBean;

@Singleton
public class StackdriverMeterRegistryProvider {
    private static final Logger log = Logger.getLogger(StackdriverMeterRegistryProvider.class);

    static final String PREFIX = "quarkus.micrometer.export.stackdriver.";
    static final String PUBLISH = "stackdriver.publish";
    static final String ENABLED = "stackdriver.enabled";

    final Instance<MeterFilter> filters;

    StackdriverMeterRegistryProvider(
            @MeterFilterConstraint(applyTo = StackdriverMeterRegistry.class) Instance<MeterFilter> filters) {
        log.debugf("StackdriverMeterRegistryProvider initialized. hasFilters=%s", !filters.isUnsatisfied());
        this.filters = filters;
    }

    @Produces
    @Singleton
    @DefaultBean
    public StackdriverConfig configure(Config config) {
        final Map<String, String> properties = MicrometerRecorder.captureProperties(config, PREFIX);

        // Special check: if publish is set, override the value of enabled
        // Specifically, the stackdriver registry must be enabled for this
        // Provider to even be present. If this instance (at runtime) wants
        // to prevent metrics from being published, then it would set
        // quarkus.micrometer.export.stackdriver.publish=false
        if (properties.containsKey(PUBLISH)) {
            properties.put(ENABLED, properties.get(PUBLISH));
        }

        return new StackdriverConfig() {
            @Override
            public String get(String key) {
                return properties.get(key);
            }
        };
    }

    @Produces
    @Singleton
    @DefaultBean
    public StackdriverMeterRegistry registry(StackdriverConfig config, Clock clock) {
        StackdriverMeterRegistry registry = new StackdriverMeterRegistry(config, clock);
        // Apply stackdriver-registry-specific meter filters
        if (!filters.isUnsatisfied()) {
            filters.stream().forEach(registry.config()::meterFilter);
        }
        return registry;
    }
}
