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
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import io.micrometer.jmx.JmxConfig;
import io.micrometer.jmx.JmxMeterRegistry;
import io.quarkus.arc.DefaultBean;

@Singleton
public class JmxMeterRegistryProvider {
    private static final Logger log = Logger.getLogger(JmxMeterRegistryProvider.class);
    static final String PREFIX = "quarkus.micrometer.export.jmx.";

    final Instance<MeterFilter> filters;

    JmxMeterRegistryProvider(@MeterFilterConstraint(type = JmxMeterRegistry.class) Instance<MeterFilter> filters) {
        log.debugf("JmxMeterRegistryProvider initialized. hasFilters=%s", !filters.isUnsatisfied());
        this.filters = filters;
    }

    @Produces
    @Singleton
    @DefaultBean
    public HierarchicalNameMapper config() {
        return HierarchicalNameMapper.DEFAULT;
    }

    @Produces
    @Singleton
    @DefaultBean
    public JmxConfig configure(Config config) {
        final Map<String, String> properties = MicrometerRecorder.captureProperties(config, PREFIX);

        return new JmxConfig() {
            @Override
            public String get(String key) {
                return properties.get(key);
            }
        };
    }

    @Produces
    @Singleton
    @DefaultBean
    public JmxMeterRegistry registry(JmxConfig config, Clock clock, HierarchicalNameMapper nameMapper) {
        JmxMeterRegistry registry = new JmxMeterRegistry(config, clock, nameMapper);

        // Apply JMX-specific meter filters
        if (!filters.isUnsatisfied()) {
            filters.stream().forEach(registry.config()::meterFilter);
        }
        return registry;
    }
}
