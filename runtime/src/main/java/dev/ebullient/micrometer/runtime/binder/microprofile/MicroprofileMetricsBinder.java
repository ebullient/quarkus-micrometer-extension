package dev.ebullient.micrometer.runtime.binder.microprofile;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNull;
import io.quarkus.arc.ArcInvocationContext;

public class MicroprofileMetricsBinder implements MeterBinder {

    @Inject
    Instance<GaugeAdapter> allGaugeAdapters;

    @Override
    public void bindTo(@NonNull MeterRegistry registry) {
        // register all annotation-declared gauges
        for (Iterator<GaugeAdapter> gauges = allGaugeAdapters.iterator(); gauges.hasNext();) {
            GaugeAdapter g = gauges.next();

            // Build and register the gauge
            Gauge.Builder<Supplier<Number>> builder = Gauge.builder(g.name(), g::getValue);
            if (g.description() != null) {
                builder.description(g.description());
            }
            if (g.tags() != null) {
                builder.tags(g.tags());
            }
            builder.strongReference(true).register(registry);
        }
    }

    static <T> T getAnnotation(InvocationContext context, Class<?> annotationClass) {
        Set<Annotation> annotations = (Set<Annotation>) context.getContextData()
                .get(ArcInvocationContext.KEY_INTERCEPTOR_BINDINGS);

        for (Annotation a : annotations) {
            if (annotationClass.isInstance(a)) {
                return (T) a;
            }
        }
        return null;
    }
}
