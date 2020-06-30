package dev.ebullient.micrometer.runtime.binder.microprofile;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Supplier;

import javax.enterprise.inject.Instance;
import javax.interceptor.InvocationContext;

import dev.ebullient.micrometer.runtime.binder.microprofile.metric.AnnotatedGaugeAdapter;
import dev.ebullient.micrometer.runtime.binder.microprofile.metric.InjectedMetricProducer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNull;
import io.quarkus.arc.ArcInvocationContext;

public class MicroprofileMetricsBinder implements MeterBinder {

    final Instance<AnnotatedGaugeAdapter> allGaugeAdapters;

    final InjectedMetricProducer metricProvider;

    MicroprofileMetricsBinder(Instance<AnnotatedGaugeAdapter> allGaugeAdapters, InjectedMetricProducer metricProvider) {
        this.allGaugeAdapters = allGaugeAdapters;
        this.metricProvider = metricProvider;
    }

    @Override
    public void bindTo(@NonNull MeterRegistry registry) {
        // register all annotation-declared gauges
        for (Iterator<AnnotatedGaugeAdapter> gauges = allGaugeAdapters.iterator(); gauges.hasNext();) {
            AnnotatedGaugeAdapter g = gauges.next();

            // Build and register the gauge
            Gauge.Builder<Supplier<Number>> builder = Gauge.builder(g.name(), g::getValue);
            if (g.description() != null) {
                builder.description(g.description());
            }
            if (g.tags() != null) {
                builder.tags(g.tags());
            }
            if (g.baseUnit() != null) {
                builder.baseUnit(g.baseUnit());
            }
            builder.strongReference(true).register(registry);
        }
    }

    static <T> T getAnnotation(InvocationContext context, Class<T> annotationClass) {
        Set<Annotation> annotations = (Set<Annotation>) context.getContextData()
                .get(ArcInvocationContext.KEY_INTERCEPTOR_BINDINGS);

        for (Annotation a : annotations) {
            if (annotationClass.isInstance(a)) {
                return annotationClass.cast(a);
            }
        }
        return null;
    }
}
