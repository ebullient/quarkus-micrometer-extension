package dev.ebullient.micrometer.runtime.binder.microprofile.metric;

import java.util.concurrent.atomic.LongAdder;

import org.eclipse.microprofile.metrics.ConcurrentGauge;

public class ConcurrentGaugeImpl implements ConcurrentGauge {
    final LongAdder longAdder = new LongAdder();

    @Override
    public long getCount() {
        return longAdder.longValue();
    }

    /**
     * Not supported for micrometer. Min/max values per dropwizard
     * would be provided by dropwizard capabilities if enabled.
     */
    @Override
    public long getMax() {
        throw new UnsupportedOperationException("This operation is not supported when used with micrometer");
    }

    /**
     * Not supported for micrometer. Min/max values per dropwizard
     * would be provided by dropwizard capabilities if enabled.
     */
    @Override
    public long getMin() {
        throw new UnsupportedOperationException("This operation is not supported when used with micrometer");
    }

    @Override
    public void inc() {
        longAdder.increment();
    }

    @Override
    public void dec() {
        longAdder.decrement();
    }
}
