package dev.ebullient.micrometer.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

@ApplicationScoped
public class CompositeMeterRegistryProvider {

    @Produces
    @Singleton
    public MeterRegistry registry(Clock clock, @Any Instance<MeterRegistry> registries) {
        System.out.println("REGISTRY MONSTER");
        return new CompositeMeterRegistry(clock, registries);
    }
}
