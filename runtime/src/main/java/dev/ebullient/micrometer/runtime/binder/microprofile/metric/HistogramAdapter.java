package dev.ebullient.micrometer.runtime.binder.microprofile.metric;

import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Snapshot;

import io.micrometer.core.instrument.DistributionSummary;

public class HistogramAdapter implements Histogram {
    final DistributionSummary summary;

    public HistogramAdapter(DistributionSummary summary) {
        this.summary = summary;
    }

    @Override
    public void update(int i) {
        summary.record(i);
    }

    @Override
    public void update(long l) {
        summary.record(l);
    }

    @Override
    public long getCount() {
        return summary.count();
    }

    /** Not supported. */
    @Override
    public Snapshot getSnapshot() {
        throw new UnsupportedOperationException("This operation is not supported when used with micrometer");
    }
}
