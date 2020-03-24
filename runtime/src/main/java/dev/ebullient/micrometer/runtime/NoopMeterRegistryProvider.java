package dev.ebullient.micrometer.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.quarkus.arc.DefaultBean;

@ApplicationScoped
public class NoopMeterRegistryProvider {

    @Produces
    @Singleton
    @DefaultBean
    public MeterRegistry registry(Clock clock) {
        System.out.println("NO_OP MONSTER");
        return new CompositeMeterRegistry(clock);
    }
}
