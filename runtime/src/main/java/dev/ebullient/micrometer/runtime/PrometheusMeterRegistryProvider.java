package dev.ebullient.micrometer.runtime;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.quarkus.arc.DefaultBean;

@Singleton
public class PrometheusMeterRegistryProvider {
    private static final Logger log = Logger.getLogger(PrometheusMeterRegistryProvider.class);

    PrometheusMeterRegistryProvider() {
        log.debug("PrometheusMeterRegistryProvider initialized");
    }

    @Produces
    @Singleton
    @DefaultBean
    public PrometheusConfig config() {
        return PrometheusConfig.DEFAULT;
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
