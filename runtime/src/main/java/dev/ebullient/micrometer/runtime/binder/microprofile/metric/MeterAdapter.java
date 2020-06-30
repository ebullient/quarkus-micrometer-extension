package dev.ebullient.micrometer.runtime.binder.microprofile.metric;

import org.eclipse.microprofile.metrics.Meter;

import io.micrometer.core.instrument.Counter;

public class MeterAdapter implements Meter {
    final Counter counter;

    MeterAdapter(Counter counter) {
        this.counter = counter;
    }

    @Override
    public void mark() {
        counter.increment();
    }

    @Override
    public void mark(long l) {
        counter.increment(l);
    }

    @Override
    public long getCount() {
        return (long) counter.count();
    }

    @Override
    public double getFifteenMinuteRate() {
        throw new UnsupportedOperationException("This operation is not supported when used with micrometer");
    }

    @Override
    public double getFiveMinuteRate() {
        throw new UnsupportedOperationException("This operation is not supported when used with micrometer");
    }

    @Override
    public double getMeanRate() {
        throw new UnsupportedOperationException("This operation is not supported when used with micrometer");
    }

    @Override
    public double getOneMinuteRate() {
        throw new UnsupportedOperationException("This operation is not supported when used with micrometer");
    }
}
