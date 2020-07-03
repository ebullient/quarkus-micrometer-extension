package dev.ebullient.micrometer.runtime.binder.mpmetrics;

import java.util.Iterator;

import javax.enterprise.inject.Instance;
import javax.inject.Singleton;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

@Singleton
class MpMetricsBinder implements MeterBinder {

    final Instance<AnnotatedGaugeAdapter> allGaugeAdapters;

    // Micrometer application meter registry
    final MpMetricsRegistry.MetricRegistryAdapter registry;

    MpMetricsBinder(MpMetricsRegistry.MetricRegistryAdapter registry,
            Instance<AnnotatedGaugeAdapter> allGaugeAdapters) {
        this.registry = registry;
        this.allGaugeAdapters = allGaugeAdapters;
    }

    @Override
    public void bindTo(MeterRegistry r) {
        // register all annotation-declared gauges
        for (Iterator<AnnotatedGaugeAdapter> gauges = allGaugeAdapters.iterator(); gauges.hasNext();) {
            registry.bindAnnotatedGauge(gauges.next());
        }
    }
}
