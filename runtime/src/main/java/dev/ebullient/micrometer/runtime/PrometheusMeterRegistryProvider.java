package dev.ebullient.micrometer.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.quarkus.arc.DefaultBean;

@ApplicationScoped
public class PrometheusMeterRegistryProvider {

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
