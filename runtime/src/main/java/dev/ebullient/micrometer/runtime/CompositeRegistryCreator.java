package dev.ebullient.micrometer.runtime;

import static javax.interceptor.Interceptor.Priority.PLATFORM_AFTER;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.quarkus.arc.AlternativePriority;

/**
 * Ensure there is one resolvable MeterRegistry.
 *
 * @return the single resolveable "root" MeterRegistry
 */
public class CompositeRegistryCreator {
    private static CompositeMeterRegistry rootRegistry;

    @Produces
    @Singleton
    @AlternativePriority(PLATFORM_AFTER)
    public MeterRegistry produceRootRegistry() {
        return rootRegistry;
    }

    public static void setRootRegistry(CompositeMeterRegistry meterRegistry) {
        rootRegistry = meterRegistry;
    }
}
