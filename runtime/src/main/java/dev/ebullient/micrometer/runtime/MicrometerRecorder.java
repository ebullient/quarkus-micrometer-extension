package dev.ebullient.micrometer.runtime;

import java.util.HashMap;
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

    public void configureRegistry(Set<String> registryTypes, ShutdownContext context) {
        log.debugf("Configuring Micrometer registries : %s", registryTypes);

        Instance<MeterRegistry> allRegistries = CDI.current().select(MeterRegistry.class, Any.Literal.INSTANCE);
        final MeterRegistry rootRegistry = allRegistries.get();

        // Customize individual filter types
        registryTypes.forEach(type -> {
            // @MeterFilterConstraint(applyTo = DatadogMeterRegistry.class) Instance<MeterFilter> filters
            Class<?> typeClass = getClassForName(type);
            if (typeClass == null) {
                log.warnf("Unable to configure %s. Class not found", type);
            } else {
                Instance<MeterFilter> classFilters = CDI.current().select(MeterFilter.class,
                        new MeterFilterConstraint.Literal(typeClass));
                Instance<?> typedRegistries = CDI.current().select(typeClass, Any.Literal.INSTANCE);

                log.debugf("Configuring %s instances. hasFilters=%s", type, !classFilters.isUnsatisfied());
                if (!classFilters.isUnsatisfied() && !typedRegistries.isUnsatisfied()) {
                    typedRegistries.forEach(registry -> {
                        classFilters.forEach(((MeterRegistry) registry).config()::meterFilter);
                    });
                }
            }
        });

        // Filters to change/constrain construction/output of metrics
        Instance<MeterFilter> filters = CDI.current().select(MeterFilter.class, Default.Literal.INSTANCE);
        log.debugf("Configuring all registries. hasFilters=%s", !filters.isUnsatisfied());
        if (!filters.isUnsatisfied()) {
            allRegistries.forEach(registry -> {
                filters.forEach(registry.config()::meterFilter);
            });
        }

        log.debugf("Configuring root registry : %s", rootRegistry);

        Instance<NamingConvention> convention = CDI.current().select(NamingConvention.class);
        if (convention.isResolvable()) {
            rootRegistry.config().namingConvention(convention.get());
        }

        // Binders must be last, apply to "top-level" registry
        Instance<MeterBinder> binders = CDI.current().select(MeterBinder.class, Any.Literal.INSTANCE);
        binders.forEach(binder -> {
            log.debugf("Binding %s", binder);
            binder.bindTo(rootRegistry);
        });

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
