package dev.ebullient.micrometer.runtime.export;

import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.Config;
import org.jboss.logging.Logger;

import dev.ebullient.micrometer.runtime.Annotations.MeterFilterConstraint;
import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.quarkus.arc.DefaultBean;

@Singleton
public class PrometheusMeterRegistryProvider {
    private static final Logger log = Logger.getLogger(PrometheusMeterRegistryProvider.class);
    static final String PREFIX = "quarkus.micrometer.export.prometheus.";

    final Instance<MeterFilter> filters;

    PrometheusMeterRegistryProvider(
            @MeterFilterConstraint(applyTo = PrometheusMeterRegistry.class) Instance<MeterFilter> filters) {
        log.debugf("PrometheusMeterRegistryProvider initialized. hasFilters=%s", !filters.isUnsatisfied());
        this.filters = filters;
    }

    @Produces
    @Singleton
    @DefaultBean
    public PrometheusConfig configure(Config config) {
        final Map<String, String> properties = MicrometerRecorder.captureProperties(config, PREFIX);

        return new PrometheusConfig() {
            @Override
            public String get(String key) {
                return properties.get(key);
            }
        };
    }

    @Produces
    @Singleton
    @DefaultBean
    public CollectorRegistry collectorRegistry() {
        return new CollectorRegistry(true);
    }

    @Produces
    @Singleton
    public PrometheusMeterRegistry registry(PrometheusConfig config, CollectorRegistry collectorRegistry, Clock clock) {
        System.out.println("Extension Prometheus Registry");
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(config, collectorRegistry, clock);
        // Apply prometheus-specific meter filters
        if (!filters.isUnsatisfied()) {
            filters.stream().forEach(registry.config()::meterFilter);
        }
        return registry;
    }
}
