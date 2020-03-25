package dev.ebullient.micrometer.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.interceptor.Interceptor;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.quarkus.arc.AlternativePriority;

@ApplicationScoped
public class CompositeMeterRegistryProvider {

    @Produces
    @Singleton
    @AlternativePriority(Interceptor.Priority.PLATFORM_AFTER)
    public CompositeMeterRegistry registry(Clock clock, @Any Instance<MeterRegistry> registries) {
        for (MeterRegistry r : registries) {
            System.out.println(r);
        }
        return new CompositeMeterRegistry(clock);
    }
}
