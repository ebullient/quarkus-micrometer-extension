package dev.ebullient.micrometer.runtime;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.quarkus.arc.DefaultBean;

@Singleton
public class NoopMeterRegistryProvider {

    @Produces
    @Singleton
    @DefaultBean
    public MeterRegistry registry(Clock clock) {
        return new CompositeMeterRegistry(clock);
    }
}
