package dev.ebullient.micrometer.deployment.binder;

import javax.inject.Inject;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Metric;

public class MpInjectColorResource {
    @Inject
    @Metric
    Counter redCount;

    @Inject
    @Metric(name = "blue")
    Counter blueCount;

    @Inject
    @Metric(absolute = true)
    Counter greenCount;

    @Inject
    @Metric(name = "purple", absolute = true)
    Counter purpleCount;
}
