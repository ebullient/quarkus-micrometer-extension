package dev.ebullient.micrometer.runtime;

import java.util.Map;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.Config;
import org.jboss.logging.Logger;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.quarkus.arc.DefaultBean;

@Singleton
public class PrometheusMeterRegistryProvider {
    private static final Logger log = Logger.getLogger(PrometheusMeterRegistryProvider.class);
    static final String PREFIX = "quarkus.micrometer.export.prometheus.";

    PrometheusMeterRegistryProvider() {
        log.debug("PrometheusMeterRegistryProvider initialized");
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
    @DefaultBean
    public PrometheusMeterRegistry registry(PrometheusConfig config, CollectorRegistry collectorRegistry, Clock clock) {
        return new PrometheusMeterRegistry(config, collectorRegistry, clock);
    }
}
