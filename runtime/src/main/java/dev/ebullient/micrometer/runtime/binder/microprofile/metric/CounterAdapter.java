package dev.ebullient.micrometer.runtime.binder.microprofile.metric;

import io.micrometer.core.instrument.Counter;

public class CounterAdapter implements org.eclipse.microprofile.metrics.Counter {

    final Counter counter;

    CounterAdapter(Counter counter) {
        this.counter = counter;
    }

    @Override
    public void inc() {
        counter.increment();
    }

    @Override
    public void inc(long l) {
        counter.increment((double) l);
    }

    @Override
    public long getCount() {
        return (long) counter.count();
    }
}
