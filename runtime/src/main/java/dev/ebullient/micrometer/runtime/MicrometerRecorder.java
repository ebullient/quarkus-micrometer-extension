package dev.ebullient.micrometer.runtime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import org.eclipse.microprofile.config.Config;
import org.jboss.logging.Logger;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.NamingConvention;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class MicrometerRecorder {
    private static final Logger log = Logger.getLogger(MicrometerRecorder.class);
    static final int TRIM_POS = "quarkus.micrometer.export.".length();

    public void configureRegistry(Set<Class<? extends MeterRegistry>> registryClasses, ShutdownContext context) {
        log.debugf("Configuring Micrometer registries : %s", registryClasses);

        Instance<MeterRegistry> allRegistries = CDI.current().select(MeterRegistry.class, Any.Literal.INSTANCE);
        final MeterRegistry rootRegistry = allRegistries.get();

        // Filters to change/constrain construction/output of metrics
        // Customize registries by class or type
        for (Class<? extends MeterRegistry> typeClass : registryClasses) {
            // @MeterFilterConstraint(applyTo = DatadogMeterRegistry.class) Instance<MeterFilter> filters
            Instance<MeterFilter> classFilters = CDI.current().select(MeterFilter.class,
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
        Instance<MeterFilter> generalFilters = CDI.current().select(MeterFilter.class, Default.Literal.INSTANCE);
        log.debugf("Configuring all registries. hasFilters=%s", !generalFilters.isUnsatisfied());
        if (!generalFilters.isUnsatisfied()) {
            for (Iterator<? extends MeterRegistry> registries = allRegistries.iterator(); registries.hasNext();) {
                for (Iterator<MeterFilter> filters = generalFilters.iterator(); filters.hasNext();) {
                    registries.next().config().meterFilter(filters.next());
                }
            }
        }

        log.debugf("Configuring root registry : %s", rootRegistry);
        Instance<NamingConvention> convention = CDI.current().select(NamingConvention.class);
        if (convention.isResolvable()) {
            rootRegistry.config().namingConvention(convention.get());
        }

        // Binders must be last, apply to "top-level" registry
        Instance<MeterBinder> allBinders = CDI.current().select(MeterBinder.class, Any.Literal.INSTANCE);
        for (Iterator<MeterBinder> binders = allBinders.iterator(); binders.hasNext();) {
            binders.next().bindTo(rootRegistry);
        }

        // Add the current CDI root registry to the global composite
        Metrics.addRegistry(rootRegistry);
        log.debug("Global metrics registry initialized");

        context.addShutdownTask(new Runnable() {
            @Override
            public void run() {
                rootRegistry.close();

                // Remove the CDI root registry from the global composite
                log.debug("Root registry removed from global registry");
                Metrics.removeRegistry(rootRegistry);
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
