package dev.ebullient.micrometer.runtime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import org.eclipse.microprofile.config.Config;
import org.jboss.logging.Logger;

import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderAdapter;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.NamingConvention;
import io.quarkus.arc.Arc;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class MicrometerRecorder {
    private static final Logger log = Logger.getLogger(MicrometerRecorder.class);
    static final int TRIM_POS = "quarkus.micrometer.export.".length();

    private volatile static CompositeMeterRegistry quarkusRegistry;

    /* STATIC_INIT */
    public RuntimeValue<MeterRegistry> createRootRegistry() {
        BeanManager beanManager = Arc.container().beanManager();

        Clock clock = beanManager.createInstance().select(Clock.class).get();
        quarkusRegistry = new CompositeMeterRegistry(clock);

        // Global registry configuration
        Instance<MeterFilter> generalFilters = beanManager.createInstance()
                .select(MeterFilter.class, Default.Literal.INSTANCE);
        log.debugf("Applying filters to root registry. hasFilters=%s", !generalFilters.isUnsatisfied());
        if (!generalFilters.isUnsatisfied()) {
            for (MeterFilter generalFilter : generalFilters) {
                quarkusRegistry.config().meterFilter(generalFilter);
            }
        }

        Instance<NamingConvention> convention = beanManager.createInstance()
                .select(NamingConvention.class);
        if (convention.isResolvable()) {
            NamingConvention nc = convention.get();
            log.debugf("Configure naming convention for root registry : %s", nc);
            quarkusRegistry.config().namingConvention(nc);
        }

        VertxMeterBinderAdapter.setMeterRegistry(quarkusRegistry);
        CompositeRegistryCreator.setRootRegistry(quarkusRegistry);
        return new RuntimeValue<>(quarkusRegistry);
    }

    /* RUNTIME_INIT */
    public void configureRegistries(Set<Class<? extends MeterRegistry>> registryClasses,
            ShutdownContext context) {
        BeanManager beanManager = Arc.container().beanManager();

        log.debugf("Configuring Micrometer registries : %s", registryClasses);

        // Find MeterFilters that configure specific registry classes, i.e.:
        // @MeterFilterConstraint(applyTo = DatadogMeterRegistry.class) Instance<MeterFilter> filters
        Map<Class<? extends MeterRegistry>, MeterFilter> classMeterFilters = new HashMap<>(registryClasses.size());
        for (Class<? extends MeterRegistry> typeClass : registryClasses) {
            Instance<MeterFilter> classFilters = beanManager.createInstance()
                    .select(MeterFilter.class, new MeterFilterConstraint.Literal(typeClass));
            if (!classFilters.isUnsatisfied()) {
                log.debugf("MeterFilter discovered for %s", typeClass);
                classMeterFilters.put(typeClass, classFilters.get());
            }
        }

        Set<Bean<?>> beans = new HashSet<>(beanManager.getBeans(MeterRegistry.class, Any.Literal.INSTANCE));
        beans.removeIf(bean -> bean.getBeanClass().equals(CompositeRegistryCreator.class));

        // Find and configure MeterRegistry beans
        for (Bean<?> i : beans) {
            MeterRegistry registry = (MeterRegistry) beanManager
                    .getReference(i, MeterRegistry.class, beanManager.createCreationalContext(i));

            // Add & configure non-root registries
            if (registry != quarkusRegistry) {
                // Apply type/class-specific MeterFilter if it exists
                MeterFilter mf = classMeterFilters.get(registry.getClass());
                if (mf != null) {
                    log.debugf("Applying MeterFilter %s to %s", registry.getClass(), registry);
                    registry.config().meterFilter(mf);
                }

                quarkusRegistry.add(registry);
            }
        }

        // Binders are added last
        Instance<MeterBinder> allBinders = beanManager.createInstance()
                .select(MeterBinder.class, Any.Literal.INSTANCE);
        for (MeterBinder binder : allBinders) {
            binder.bindTo(quarkusRegistry);
        }

        // Add the root registry to the global composite
        Metrics.addRegistry(quarkusRegistry);
        log.debug("Global metrics registry initialized");

        context.addShutdownTask(new Runnable() {
            @Override
            public void run() {
                quarkusRegistry.close();

                // Remove the CDI root registry from the global composite
                log.debug("Root registry removed from global registry");
                Metrics.removeRegistry(quarkusRegistry);
            }
        });
    }

    public static Map<String, String> captureProperties(Config config, String prefix) {
        final Map<String, String> properties = new HashMap<>();

        // Rename and store stackdriver properties
        for (String name : config.getPropertyNames()) {
            if (name.startsWith(prefix)) {
                String key = convertKey(name);
                String value = config.getValue(name, String.class);
                properties.put(key, value);
            }
        }
        return properties;
    }

    static String convertKey(String name) {
        String key = name.substring(TRIM_POS);
        key = MicrometerRecorder.camelHumpify(key);
        return key;
    }

    static String camelHumpify(String s) {
        if (s.indexOf('-') >= 0) {
            StringBuilder b = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '-') {
                    i++;
                    if (i < s.length()) {
                        b.append(Character.toUpperCase(s.charAt(i)));
                    }
                } else {
                    b.append(s.charAt(i));
                }
            }
            return b.toString();
        }
        return s;
    }

    public static Class<?> getClassForName(String classname) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(classname, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
        }
        log.debugf("getClass: TCCL: %s ## %s : %s", Thread.currentThread().getContextClassLoader(), classname, (clazz != null));
        return clazz;
    }
}
