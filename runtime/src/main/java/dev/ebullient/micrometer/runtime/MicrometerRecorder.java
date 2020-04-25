package dev.ebullient.micrometer.runtime;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Any;
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

    public void configureRegistry(ShutdownContext context) {
        final MeterRegistry registry = CDI.current().select(MeterRegistry.class).get();

        // Filters to change/constrain construction/output of metrics
        Instance<MeterFilter> filters = CDI.current().select(MeterFilter.class, Any.Literal.INSTANCE);
        log.debugf("Configuring global registry. hasFilters=%s", !filters.isUnsatisfied());
        if (!filters.isUnsatisfied()) {
            filters.forEach((x) -> System.out.println(x.getClass() + " : @" + System.identityHashCode(x)));
        }

        Instance<NamingConvention> convention = CDI.current().select(NamingConvention.class, Any.Literal.INSTANCE);
        if (convention.isResolvable()) {
            registry.config().namingConvention(convention.get());
        }

        // Binders must be last, apply to "top-level" registry
        Instance<MeterBinder> binders = CDI.current().select(MeterBinder.class, Any.Literal.INSTANCE);
        binders.stream().forEach(binder -> binder.bindTo(registry));

        // Add the current CDI root registry to the global composite
        Metrics.addRegistry(registry);
        log.debug("Global metrics registry initialized");

        context.addShutdownTask(new Runnable() {
            @Override
            public void run() {
                registry.close();

                // Remove the CDI root registry from the global composite
                log.debug("CDI registry removed from global registry");
                Metrics.removeRegistry(registry);
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
