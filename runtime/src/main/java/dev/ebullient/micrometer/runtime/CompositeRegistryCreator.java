package dev.ebullient.micrometer.runtime;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.quarkus.arc.BeanCreator;

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
public class CompositeRegistryCreator implements BeanCreator<MeterRegistry> {
    private static final Logger log = Logger.getLogger(CompositeRegistryCreator.class);

    @Override
    public MeterRegistry create(CreationalContext<MeterRegistry> creationalContext, Map<String, Object> params) {
        log.debugf("Create default or aggregating composite for %s", params);
        System.out.printf("Create default or aggregating composite for %s\n", params);

        Clock clock = CDI.current().select(Clock.class).get();
        BeanManager beanManager = CDI.current().getBeanManager();

        // Find all of the registered/created registry beans of whatever types were found
        Set<MeterRegistry> registries = new HashSet<>();
        params.values().forEach(v -> {
            Class<?> type = (Class<?>) v;
            Set<Bean<?>> beans = beanManager.getBeans(type, new Any.Literal());
            System.out.println(beans);
            // for (Bean<?> bean : beans) {
            //     registries.add((MeterRegistry) beanManager.getReference(bean, type, creationalContext));
            // }
            // if (instances.isUnsatisfied()) {
            //     log.debugf("no instances of %s", type);
            // } else {
            //     instances.forEach(x -> registries.add((MeterRegistry) x));
            // }
        });

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
