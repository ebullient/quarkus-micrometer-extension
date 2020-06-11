package dev.ebullient.micrometer.runtime;

import static javax.interceptor.Interceptor.Priority.PLATFORM_AFTER;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.quarkus.arc.AlternativePriority;

/**
 * Ensure there is one resolvable MeterRegistry.
 * <p>
 * Fetch MeterRegistry instances using types discovered from registered Producer fields and methods.
 * If there are no instances, create a no-op/empty CompositeMeterRegistry. If there are several instances,
 * create a CompositeMeterRegistry that aggregates the collection. Otherwise, return the single discovered
 * bean instance.
 *
 * @return the single resolveable "root" MeterRegistry
 */
public class CompositeRegistryCreator {
    private static final Logger log = Logger.getLogger(CompositeRegistryCreator.class);

    public static CompositeMeterRegistry rootRegistry;

    @Produces
    @Singleton
    @AlternativePriority(PLATFORM_AFTER)
    public MeterRegistry produceRootRegistry() {
        return rootRegistry;
    }

}
