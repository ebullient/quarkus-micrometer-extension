package dev.ebullient.micrometer.runtime.binder.microprofile.metric;

import io.micrometer.core.instrument.Gauge;

public class GaugeAdapter<T> implements org.eclipse.microprofile.metrics.Gauge<T> {
    final Gauge gauge;
    final Class<T> type;

    GaugeAdapter(Gauge gauge, Class<T> type) {
        this.gauge = gauge;
        this.type = type;
        if (type.isAssignableFrom(Number.class)) {
            throw new IllegalArgumentException("Gauge must return a Number type");
        }
    }

    @Override
    public T getValue() {
        return type.cast(gauge.value());
    }
}
