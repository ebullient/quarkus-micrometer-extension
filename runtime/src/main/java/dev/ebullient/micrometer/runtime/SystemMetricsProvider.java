package dev.ebullient.micrometer.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;

@ApplicationScoped
public class SystemMetricsProvider {

    @Produces
    @Singleton
    @Default
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }

    @Produces
    @Singleton
    @Default
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    @Produces
    @Singleton
    @Default
    public FileDescriptorMetrics fileDescriptorMetrics() {
        return new FileDescriptorMetrics();
    }

}
