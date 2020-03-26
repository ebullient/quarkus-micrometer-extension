package dev.ebullient.micrometer.runtime;

import java.util.function.Function;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;

import org.jboss.logging.Logger;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

@Recorder
public class MicrometerRecorder {
    private static final Logger LOGGER = Logger.getLogger(MicrometerRecorder.class.getName());

    public Function<Router, Route> route(String name) {
        return new Function<Router, Route>() {
            @Override
            public Route apply(Router router) {
                return router.route(name);
            }
        };
    }

    public void configureRegistry() {
        MeterRegistry registry = CDI.current().select(MeterRegistry.class).get();

        // Filters to change/constrain construction/output of metrics
        // Instance<MeterFilter> filters = CDI.current().select(MeterFilter.class, Any.Literal.INSTANCE);
        // filters.stream().forEach(registry.config()::meterFilter);

        // Binders must be last
        Instance<MeterBinder> binders = CDI.current().select(MeterBinder.class, Any.Literal.INSTANCE);
        binders.stream().forEach(binder -> binder.bindTo(registry));
    }
}
