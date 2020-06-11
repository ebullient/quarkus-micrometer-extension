package dev.ebullient.micrometer.runtime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class MicrometerRecorder {
    private static final Logger log = Logger.getLogger(MicrometerRecorder.class);
    static final int TRIM_POS = "quarkus.micrometer.export.".length();

    private volatile static CompositeMeterRegistry quarkusRegistry;

    public void createRootRegistry() {
        Clock clock = Arc.container().beanManager().createInstance().select(Clock.class).get();
        quarkusRegistry = new CompositeMeterRegistry(clock);
        VertxMeterBinderAdapter.meterRegistry.set(quarkusRegistry);
        CompositeRegistryCreator.rootRegistry = quarkusRegistry;
    }

    public void configureRegistry(Set<Class<? extends MeterRegistry>> registryClasses,
            ShutdownContext context) {
        BeanManager beanManager = Arc.container().beanManager();
        Set<Bean<?>> beans = new HashSet<>(beanManager.getBeans(MeterRegistry.class, Any.Literal.INSTANCE));
        beans.removeIf(bean -> bean.getBeanClass().equals(CompositeRegistryCreator.class));

        // Find all of the registered/created registry beans of whatever types were found
        for (Bean<?> i : beans) {
            MeterRegistry registry = (MeterRegistry) beanManager.getReference(i, MeterRegistry.class,
                    beanManager.createCreationalContext(i));

            if (registry != quarkusRegistry) {
                quarkusRegistry.add(registry);
            }
        }

        log.debugf("Configuring Micrometer registries : %s", registryClasses);

        Instance<MeterRegistry> allRegistries = Arc.container().beanManager().createInstance()
                .select(MeterRegistry.class, Any.Literal.INSTANCE);

        // Filters to change/constrain construction/output of metrics
        // Customize registries by class or type
        for (Class<? extends MeterRegistry> typeClass : registryClasses) {
            // @MeterFilterConstraint(applyTo = DatadogMeterRegistry.class) Instance<MeterFilter> filters
            Instance<MeterFilter> classFilters = Arc.container().beanManager().createInstance().select(
                    MeterFilter.class,
                    new MeterFilterConstraint.Literal(typeClass));
            Instance<? extends MeterRegistry> typedRegistries = allRegistries.select(typeClass, Any.Literal.INSTANCE);

            log.debugf("Configuring %s instances. hasFilters=%s", typeClass, !classFilters.isUnsatisfied());
            if (!classFilters.isUnsatisfied() && !typedRegistries.isUnsatisfied()) {
                for (Iterator<? extends MeterRegistry> registries = typedRegistries.iterator(); registries.hasNext();) {
                    for (Iterator<MeterFilter> filters = classFilters.iterator(); filters.hasNext();) {
                        registries.next().config().meterFilter(filters.next());
                    }
                }
            }
        }

        // Customize all registries (global/common tags, e.g.)
        Instance<MeterFilter> generalFilters = Arc.container().beanManager().createInstance().select(MeterFilter.class,
                Default.Literal.INSTANCE);
        log.debugf("Configuring root registry filters. hasFilters=%s", !generalFilters.isUnsatisfied());
        if (!generalFilters.isUnsatisfied()) {
            for (MeterFilter generalFilter : generalFilters) {
                quarkusRegistry.config().meterFilter(generalFilter);
            }
        }

        log.debugf("Configuring root registry : %s", quarkusRegistry);
        Instance<NamingConvention> convention = Arc.container().beanManager().createInstance()
                .select(NamingConvention.class);
        if (convention.isResolvable()) {
            quarkusRegistry.config().namingConvention(convention.get());
        }

        // Binders must be last, apply to "top-level" registry
        Instance<MeterBinder> allBinders = Arc.container().beanManager().createInstance().select(MeterBinder.class,
                Any.Literal.INSTANCE);
        for (MeterBinder binder : allBinders) {
            binder.bindTo(quarkusRegistry);
        }

        // Add the current root registry to the global composite
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
        try {
            log.debug("getClass: TCCL: " + Thread.currentThread().getContextClassLoader() + " ## " + classname);
            return Class.forName(classname, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            log.debug("getClass: TCCL: " + Thread.currentThread().getContextClassLoader() + " ## " + classname + ": false");
            return null;
        }
    }

}
