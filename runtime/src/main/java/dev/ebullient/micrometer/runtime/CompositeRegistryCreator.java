package dev.ebullient.micrometer.runtime;

import static javax.interceptor.Interceptor.Priority.PLATFORM_AFTER;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Singleton;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.quarkus.arc.AlternativePriority;

/**
 * Ensure there is one resolvable MeterRegistry.
 * <p>
 * This is invoked/created by MicrometerProcessor#createRootRegistry.
 * Fetch MeterRegistry instances using types discovered from registered Producer fields and methods.
 * If there are no instances, create a no-op/empty CompositeMeterRegistry. If there are several instances,
 * create a CompositeMeterRegistry that aggregates the collection. Otherwise, return the single discovered
 * bean instance.
 *
 * @return the single resolveable "root" MeterRegistry
 */
@Dependent
public class CompositeRegistryCreator {
    private static final Logger log = Logger.getLogger(CompositeRegistryCreator.class);

    @Produces
    @Singleton
    @Alternative()
    @AlternativePriority(PLATFORM_AFTER)
    public MeterRegistry create() {

        Clock clock = CDI.current().select(Clock.class).get();
        BeanManager beanManager = CDI.current().getBeanManager();
        Set<Bean<?>> beans = new HashSet<>(beanManager.getBeans(MeterRegistry.class, Any.Literal.INSTANCE));
        Iterator<Bean<?>> it = beans.iterator();
        while (it.hasNext()) {
            if (it.next().getBeanClass().equals(CompositeRegistryCreator.class)) {
                it.remove();
            }
        }

        // Find all of the registered/created registry beans of whatever types were found
        Set<MeterRegistry> registries = new HashSet<>();
        for (Bean<?> i : beans) {
            registries.add(
                    (MeterRegistry) beanManager.getReference(i, MeterRegistry.class, beanManager.createCreationalContext(i)));
        }

        log.debugf("MeterRegistry instances discovered: %s", registries);
        System.out.printf("MeterRegistry instances discovered: %s\n", registries);

        if (registries.isEmpty()) {
            return new CompositeMeterRegistry(clock);
        } else if (registries.size() > 1) {
            return new CompositeMeterRegistry(clock, registries);
        }

        // Only one other registry was discovered. Do not create this bean.
        return registries.iterator().next();
    }

}
