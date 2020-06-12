package dev.ebullient.micrometer.runtime.binder.microprofile;

import java.util.Iterator;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNull;

public class MicroprofileMetricsBinder implements MeterBinder {

    @Inject
    Instance<GaugeAdapter> allGaugeAdapters;

    @Override
    public void bindTo(@NonNull MeterRegistry registry) {
        // register all annotation-declared gauges
        for (Iterator<GaugeAdapter> gauges = allGaugeAdapters.iterator(); gauges.hasNext();) {
            GaugeAdapter g = gauges.next();

            // Build and register the gauge
            Gauge.Builder builder = Gauge.builder(g.name(), g::getValue);
            if (g.description() != null) {
                builder.description(g.description());
            }
            if (g.tags() != null) {
                builder.tags(g.tags());
            }
            builder.strongReference(true).register(registry);
        }

        // register all annotation-declared counted methods
    }

}
