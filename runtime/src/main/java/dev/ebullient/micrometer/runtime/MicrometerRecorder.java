package dev.ebullient.micrometer.runtime;

import java.util.function.Function;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

@Recorder
public class MicrometerRecorder {
    private static final Logger log = Logger.getLogger(MicrometerRecorder.class);

    public Function<Router, Route> route(String name) {
        return router -> router.route(name);
    }

    public void configureRegistry(ShutdownContext context) {
        final MeterRegistry registry = CDI.current().select(MeterRegistry.class).get();

        // Filters to change/constrain construction/output of metrics
        // Instance<MeterFilter> filters = CDI.current().select(MeterFilter.class, Any.Literal.INSTANCE);
        // filters.stream().forEach(registry.config()::meterFilter);

        // Binders must be last: bind to global registry
        Instance<MeterBinder> binders = CDI.current().select(MeterBinder.class, Any.Literal.INSTANCE);
        binders.stream().forEach(binder -> binder.bindTo(Metrics.globalRegistry));

        // Add the current CDI root registry to the global composite
        Metrics.addRegistry(registry);
        log.debug("Global metrics registry initialized");

        context.addShutdownTask(new Runnable() {
            @Override
            public void run() {
                // Remove the CDI root registry from the global composite
                log.debug("CDI registry removed from global registry");
                Metrics.removeRegistry(registry);
            }
        });
    }

    public static String camelHumpify(String s) {
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
